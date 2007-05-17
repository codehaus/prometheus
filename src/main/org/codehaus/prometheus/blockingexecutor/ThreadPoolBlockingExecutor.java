/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.threadpool.StandardThreadPool;
import org.codehaus.prometheus.threadpool.ThreadPool;
import org.codehaus.prometheus.threadpool.ThreadPoolState;
import org.codehaus.prometheus.threadpool.WorkerJob;
import org.codehaus.prometheus.util.ConcurrencyUtil;

import static java.lang.String.format;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;

/**
 * An implementation of an {@link BlockingExecutorService} that uses a {@link ThreadPool} for thread
 * management.
 * <p/>
 * Als de threadpool gaat shut downen, krijg je geen garantie dat het werk dat uitstaat nog uitgevoerd
 * worden. Als je er werk in zet, en je ziet dat de
 * <p/>
 * Out of order execution with executors. Event though a blocking queue is used to store the items,
 * and an item won't be returned out of order (FIFO contract) it could be that:
 * -a task takes a longer time than others to execute
 * -a taken task hasn't had the time to execute (maybe unlucky context switches).
 * Fifo contract can be broken, check the PriorityBlockingQueue. Onlky when there is a single thread
 * you get the fifo guarantee.
 * <p/>
 * What happens if there are no threads in the threadpool, and the blocking executor is shut down.
 * New work won't be accepted, but outstanding work is not processed and new worker-thread creation
 * is not allowed meaning that there could be a deadlock: the blocking executor can't be shut down.
 * <p/>
 * If a task placement is started before a shutdown, but completes after the system is shutting down,
 * the placing thread is responsible to make sure that the task is processed. This is done by removing
 * the task from the queue if it is still there and throwing a RejectedExecutionException or if the
 * task isn't on the queue, it is (being) processed.-g-get
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor implements BlockingExecutorService {

    public static ThreadPool createDefaultThreadPool(ThreadFactory threadFactory, int poolsize) {
        return new StandardThreadPool(poolsize, threadFactory);
    }

    public static BlockingQueue<Runnable> createDefaultWorkQueue() {
        return new LinkedBlockingQueue<Runnable>();
    }

    public static ThreadPool createDefaultThreadPool(int poolsize) {
        return new StandardThreadPool(poolsize);
    }

    private final ThreadPool threadPool;
    private final BlockingQueue<Runnable> workQueue;

    /**
     * Creates a ThreadPoolBlockingExecutor with the given number of threads.
     *
     * @param poolsize the initial number of threads in the ThreadPool.
     * @throws IllegalArgumentException if poolsize smaller than 0.
     */
    public ThreadPoolBlockingExecutor(int poolsize) {
        this(createDefaultThreadPool(poolsize), createDefaultWorkQueue());
    }

    /**
     *
     * @param poolsize
     * @param factory
     * @param workQueue
     */
    public ThreadPoolBlockingExecutor(int poolsize,ThreadFactory factory, BlockingQueue<Runnable> workQueue) {
        this(createDefaultThreadPool(factory, poolsize), workQueue);
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
        threadPool.shutdown();
    }

    public List<Runnable> shutdownNow() {
        //the shutdown action is atomic.
        ThreadPoolState previousState = threadPool.shutdownNow();

        //at most once the previous state can be started
        if (previousState != ThreadPoolState.started) {
            //the threadpool already is shutting down, or is shut down.
            //We don't drain the queue here because the queue still can contain
            //runnable's to execute (some of these runnable's could be termination-runnable's.
            //so we don't want to loose them.
            return Collections.EMPTY_LIST;
        }

        //this is the first call to the shutdownNow method, so return the unprocessed tasks
        return drainWorkQueue();
    }

    private List<Runnable> drainWorkQueue() {
        List<Runnable> runnables = new ArrayList<Runnable>();
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

        assertPoolIsStarted();
        workQueue.put(task);
        ensureTaskHandeled(task);
    }

    public void tryExecute(Runnable task, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (task == null || unit == null) throw new NullPointerException();

        ConcurrencyUtil.ensureNoTimeout(timeout);

        assertPoolIsStarted();
        if (!workQueue.offer(task, timeout, unit))
            throw new TimeoutException();
        ensureTaskHandeled(task);
    }

    private void assertPoolIsStarted() {
        if (threadPool.getState() != ThreadPoolState.started)
            throw new RejectedExecutionException();
    }

    /**
     * Logic inside this method has been inspired by the ThreadPoolExecutor.
     *
     * @param task the task to ensure to be handled.
     */
    private void ensureTaskHandeled(Runnable task) {
        boolean shutdownOccurredWhileWaiting = false;

        Lock lock = threadPool.getStateChangeLock();
        lock.lock();
        try {
            //the following sequence of actions could have occurred:
            //-a shutdown was initiated before lock.unlock
            //-a shutdown is initiated after lock.unlock.
            //(shutdown can't happen concurrently because shutdown depends on the lock also)
            //We are not interrested in the second case, but it is up to shutdown to deal
            //with all jobs that were placed on the queue. We are only interrested in the
            //first case: job was placed, but maybe the shutdown was initiated in the meanwhile.

            if (threadPool.getState() != ThreadPoolState.started) {
                //the shutdown was initiated before the lock was obtained, so
                //this call should try to remove the task because no guarantees
                //are given that is ever is going to be processed.
                shutdownOccurredWhileWaiting = true;
            }
        } finally {
            lock.unlock();
        }

        if (shutdownOccurredWhileWaiting) {
            //the structure was shutdown, and we don't get any guarantee that the item is going to
            //be processed or dealt with. That is why we need to check if it is still there.
            if (workQueue.remove(task)) {
                //the task was still on the queue, it is removed now, so lets throw an
                //rejected execution exception to indicate that the execution has failed.
                throw new RejectedExecutionException();
            } else {
                //we were lucky, the task could not be found on the queue anymore, meaning
                //that is was handeled (either because it was returned by the shutdownNow method
                //or because it is (being) processed.                 
            }
        }
    }

    private class WorkerJobImpl implements WorkerJob<Runnable> {

        public Runnable getWork() throws InterruptedException {
            return workQueue.take();
        }

        public Runnable getWorkWhileShuttingdown() throws InterruptedException {
            return workQueue.poll(0, TimeUnit.NANOSECONDS);
        }

        public void runWork(Runnable task) {
            task.run();
        }
    }
}
