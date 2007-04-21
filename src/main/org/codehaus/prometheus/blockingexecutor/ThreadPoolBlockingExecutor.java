/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.util.StandardThreadFactory;
import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.threadpool.ThreadPool;
import org.codehaus.prometheus.threadpool.ThreadPoolState;
import org.codehaus.prometheus.threadpool.WorkerJob;
import org.codehaus.prometheus.threadpool.StandardThreadPool;

import java.util.*;
import java.util.concurrent.*;
import static java.lang.String.format;

/**
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor implements BlockingExecutorService {

    public static ThreadFactory createDefaultThreadFactory() {
        return new StandardThreadFactory();
    }

    public static ThreadPool createDefaultThreadPool(ThreadFactory threadFactory, int poolsize) {
        return new StandardThreadPool(poolsize,threadFactory);
    }

    public static BlockingQueue<Runnable> createDefaultWorkQueue() {
        return new LinkedBlockingQueue<Runnable>();
    }

    public static ThreadPool createDefaultThreadPool(int poolsize) {
        return new StandardThreadPool(poolsize);
    }

    private final ThreadPool threadPool;
    private final BlockingQueue<Runnable> workQueue;

    public ThreadPoolBlockingExecutor(int poolsize) {
        this(createDefaultThreadPool(poolsize), createDefaultWorkQueue());
    }

    public ThreadPoolBlockingExecutor(ThreadFactory factory, int poolsize, BlockingQueue<Runnable> workQueue) {
        this(createDefaultThreadPool(factory,poolsize),workQueue);
    }

    public ThreadPoolBlockingExecutor(ThreadPool threadPool, BlockingQueue<Runnable> workQueue) {
        if (threadPool == null || workQueue == null) throw new NullPointerException();
        this.threadPool = threadPool;
        this.workQueue = workQueue;
        this.threadPool.setWorkerJob(new WorkerJobImpl());
    }

    public ThreadPool getThreadPool() {
        return threadPool;
    }

    public BlockingQueue<Runnable> getWorkQueue() {
        return workQueue;
    }

    public void start() {
        threadPool.start();
    }

    public int getActualPoolSize() {
        return threadPool.getActualPoolSize();
    }

    public int getDesiredPoolSize() {
        return threadPool.getDesiredPoolSize();
    }

    public void setDesiredPoolSize(int poolSize) {
        threadPool.setDesiredPoolsize(poolSize);
    }

    public ExceptionHandler getExceptionHandler() {
        return threadPool.getExceptionHandler();
    }

    public void setExceptionHandler(ExceptionHandler handler) {
        threadPool.setExceptionHandler(handler);
    }

    public void shutdown() {
        ThreadPoolState oldState = threadPool.shutdown();
        if (oldState != ThreadPoolState.started)
            return;

        //Guarantees:
        //-only the calling thread is executing the shutdown (other threads are returned)
        //-the threadpoolrepeater is in the shutting down state
        //-there are one or more threads active
        //-these threads won't terminate themself

        //Todo:
        //we need a way to let the workers terminate.
        //Problem:
        //-if a termination task is placed on the workqueue, it could block indefinitly.
    }

    public List<Runnable> shutdownNow() {
        ThreadPoolState oldState = threadPool.shutdown();
        if (oldState != ThreadPoolState.started) {
            //the threadpool already is shutting down, or is shut down.
            //We don't drain the queue here because the queue still can contain
            //runnable's to execute (some of these runnable's could be termination-runnable's.
            //so we don't want to loose them.s
            return Collections.EMPTY_LIST;
        }

        //this is the first call to the shutdownNow method.
        return drainWorkQueue();
    }

    private List<Runnable> drainWorkQueue() {
        List<Runnable> runnables = new ArrayList();
        workQueue.drainTo(runnables);
        return runnables;
    }

    public BlockingExecutorServiceState getState() {
        ThreadPoolState state = threadPool.getState();
        switch (state) {
            case unstarted:
                return BlockingExecutorServiceState.Unstarted;
            case started:
                return BlockingExecutorServiceState.Running;
            case shuttingdown:
                return BlockingExecutorServiceState.Shuttingdown;
            case shutdown:
                return BlockingExecutorServiceState.Shutdown;
            default:
                throw new RuntimeException(format("unhandeled threadpoolstate %s", state));
        }
    }

    public void awaitShutdown() throws InterruptedException {
        threadPool.awaitShutdown();
    }

    public void tryAwaitShutdown(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        threadPool.tryAwaitShutdown(timeout, unit);
    }

    public void execute(Runnable task) throws InterruptedException {
        if (task == null) throw new NullPointerException();

        if (threadPool.getState() != ThreadPoolState.started)
            throw new RejectedExecutionException();

        workQueue.put(task);
        ensureTaskHandeled(task);
    }

    private void ensureTaskHandeled(Runnable task) {
        //it could be that the executor is shutting down.

        //this logic has been copied from the ThreadPoolExecutor.
        if (threadPool.getState() != ThreadPoolState.started) {
            if (workQueue.remove(task)) {
                //the task was still on the queue, it is removed now, so lets throw an
                //rejected execution exception to indicate that the execution has failed.
                throw new RejectedExecutionException();
            } else {
                //the task is being processed, so it is handled.
            }
        }
        //at this point the threadpool executor could be shut down, but when it shuts down,
        //the receiver will return the list of remaining runnable's.
    }

    public void tryExecute(Runnable task, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (!workQueue.offer(task, timeout, unit))
            throw new TimeoutException();
    }

    private class WorkerJobImpl implements WorkerJob<Runnable> {

        public Runnable getTask() throws InterruptedException {
            return workQueue.take();
        }

        public boolean executeTask(Runnable task) {
            task.run();
            return true;
        }
    }
}
