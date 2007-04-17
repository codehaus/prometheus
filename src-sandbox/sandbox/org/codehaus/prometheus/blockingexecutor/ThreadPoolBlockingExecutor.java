/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import static org.codehaus.prometheus.util.ConcurrencyUtil.toUsableNanos;
import org.codehaus.prometheus.util.StandardThreadFactory;
import org.codehaus.prometheus.util.Latch;
import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor implements BlockingExecutorService {

    public static ThreadFactory createDefaultThreadFactory() {
        return new StandardThreadFactory();
    }

    public static BlockingQueue<Runnable> createDefaultWorkQueue() {
        return new LinkedBlockingQueue<Runnable>();
    }

    private final ThreadFactory threadFactory;
    private final BlockingQueue<Runnable> workQueue;
    //todo: at the moment the mainLock is create internal, maybe inject it?
    private final Lock mainLock = new ReentrantLock();
    private final Latch shutdownLatch = new Latch(mainLock);
    private final Set<Thread> threadpool = new HashSet<Thread>();
    private volatile BlockingExecutorServiceState state;
    private volatile int poolsize;

    /**
     * Constructs a new ThreadPoolBlockingExecutor with a unbounded workqueue
     * and a default {@link StandardThreadFactory}.
     *
     * @param poolsize the initial number of threads in the threadpool.
     * @throws IllegalArgumentException if poolsize smaller than zero.
     */
    public ThreadPoolBlockingExecutor(int poolsize) {
        this(createDefaultThreadFactory(), createDefaultWorkQueue(), poolsize);
    }

    /**
     * Constructs a new ThreadPoolBlockingExecutor.
     *
     * @param threadFactory     the ThreadFactory being used to create threads
     * @param workQueue
     * @param poolsize                  the initial number of threads in the threadpool
     * @throws NullPointerException     if threafactory or workqueue is null.
     * @throws IllegalArgumentException if poolsize smaller than zero.
     */
    public ThreadPoolBlockingExecutor(ThreadFactory threadFactory, BlockingQueue<Runnable> workQueue, int poolsize) {
        if (threadFactory == null || workQueue == null) throw new NullPointerException();
        if (poolsize <= 0) throw new IllegalArgumentException();

        this.threadFactory = threadFactory;
        this.workQueue = workQueue;
        this.state = BlockingExecutorServiceState.Unstarted;
        this.poolsize = poolsize;
    }

    /**
     * Returns the number of 'desired' threads in this ThreadPoolBlockingExecutor.
     * The actual number of threads doesn't have to match the desired number
     * because the it could be that threads are removed from the pool, of
     * added to it.
     *
     * @return the number of 'desired' threads in the ThreadPoolBlockingExecutor.
     */
    public int getPoolSize() {
        return poolsize;
    }

    public BlockingExecutorServiceState getState() {
        return state;
    }

    /**
     * Returns the ThreadFactory used for filling the thread pool.
     *
     * @return the ThreadFactory used for filling the thread pool.
     */
    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    /**
     * Returns the work queue.
     * <p/>
     * todo:
     * remark about adding items in the queue directly.
     *
     * @return the workqueue.
     */
    public BlockingQueue<Runnable> getWorkQueue() {
        return workQueue;
    }

    /**
     * Returns the main Lock.
     *
     * @return the main Lock.
     */
    public Lock getMainLock() {
        return mainLock;
    }

    /**
     * Returns the Latch that is used for waiting for the shutdown.
     * Methods on the Latch should not be called directly, especially
     * the open methods.
     *
     * @return returns the Latch that is used for shutdown.
     */
    public Latch getShutdownLatch() {
        return shutdownLatch;
    }


    public ExceptionHandler getExceptionHandler() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Updates the state of this BlockingExecutor. All changes
     * of the state, should be done by this method.
     *
     * This call should only be made when the mainLock is held.
     *
     * @param newState
     */
    private void updateState(BlockingExecutorServiceState newState){
        if(newState.equals(BlockingExecutorServiceState.Shutdown))
            shutdownLatch.openWithoutLocking();

        this.state = newState;
    }

    public void start() {
        mainLock.lock();
        try {
            switch (state) {
                case Unstarted:
                    updateState(BlockingExecutorServiceState.Running);
                    initializePool();
                    break;
                case Running:
                    //ignore the start call, this executor already is started.
                    break;
                case Shuttingdown:{
                    String msg ="Can't start a shutting down ThreadPoolBlockingExecutor";
                    throw new IllegalStateException(msg);
                }
                case Shutdown:{
                    String msg ="Can't start a shutdown ThreadPoolBlockingExecutor";
                    throw new IllegalStateException(msg);
                }
                default:
                    throw new RuntimeException("unhandeled state:" + state);
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Initializes the pool.
     *
     * The mainLock must be held when this call is made.
     */
    private void initializePool() {
        for (int k = 0; k < poolsize; k++) {
            Worker worker = new Worker();
            Thread thread = threadFactory.newThread(worker);
            worker.thread = thread;
            thread.start();
        }
    }

    public void shutdown() {
        mainLock.lock();
        try {
            switch (state) {
                case Unstarted:
                    internalShutdown();
                    break;
                case Running:
                    internalShutdown();
                    break;
                case Shuttingdown:
                    //nothing needs to be done because it already is shutting down
                    return;
                case Shutdown:
                    //nothing needs to be done because it already is shut down
                    return;
                default:
                    throw new RuntimeException("unhandeled state:" + state);
            }
        } finally {
            mainLock.unlock();
        }
    }


    public List<Runnable> shutdownNow() {
        mainLock.lock();
        try {
            switch (state) {
                case Unstarted:
                    throw new RuntimeException("unhandeled state: " + state);
                case Running:
                    throw new RuntimeException("unhandeled state: " + state);
                case Shuttingdown:
                    return new LinkedList<Runnable>();
                case Shutdown:
                    return new LinkedList<Runnable>();
                default:
                    throw new RuntimeException("unhandeled state: " + state);
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     *
     * Can only be called if the mainLock is held.
     */
    private void internalShutdown() {
        if (threadpool.isEmpty()) {
            //if there are no workers, this BlockingExecutor is completely shutdown.
            updateState(BlockingExecutorServiceState.Shutdown);
        } else {
            //if there are workers, the BlockingExecutor goes into the Shuttingdown
            //state. It is up to the last worker, to set this BlockingExecutor into
            //the Shutdown state.
            updateState(BlockingExecutorServiceState.Shuttingdown);

            //todo: interrupt with every shutdown? Not just the shutdown??
            for (Thread thread : threadpool)
                thread.interrupt();
        }
    }

    public void awaitShutdown() throws InterruptedException {
        shutdownLatch.await();
    }

    public void tryAwaitShutdown(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        shutdownLatch.tryAwait(timeout, unit);
    }

    public void execute(Runnable task) throws InterruptedException {
        if (task == null) throw new NullPointerException();

        mainLock.lockInterruptibly();
        try {
            switch (state) {
                case Unstarted:
                    throw new RejectedExecutionException();
                case Running:
                    workQueue.put(task);
                    break;
                case Shuttingdown:
                    throw new RejectedExecutionException();
                case Shutdown:
                    throw new RejectedExecutionException();
                default:
                    throw new RuntimeException("unhandled state: " + state);
            }
        } finally {
            mainLock.unlock();
        }
    }

    public void tryExecute(Runnable task, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (task == null) throw new NullPointerException();

        long timeoutNs = toUsableNanos(timeout, unit);

        mainLock.lockInterruptibly();
        try {
            switch (state) {
                case Unstarted:
                    throw new RejectedExecutionException();
                case Running:
                    throw new RuntimeException();
                case Shuttingdown:
                    throw new RejectedExecutionException();
                case Shutdown:
                    throw new RejectedExecutionException();
                default:
                    throw new RuntimeException("unhandled state: " + state);
            }
        } finally {
            mainLock.unlock();
        }
    }


    /**
     * Returns the actual number of threads in this ThreadPoolBlockingExecutor.
     *
     * If the interrupt flag is set, it remains set.
     *
     * @return the actual number of threads in this ThreadPoolBlockingExecutor.
     */
    public long getActualPoolSize() {
        mainLock.lock();
        try {
            return threadpool.size();
        } finally {
            mainLock.unlock();
        }
    }

    private class Worker implements Runnable {

        volatile Thread thread;

        public void run() {
            try {
                for (; ;) {
                    try {
                        Runnable task = workQueue.take();
                        mainLock.lockInterruptibly();
                        try {
                            if (state.equals(BlockingExecutorServiceState.Shuttingdown))
                                throw new ShutdownException();
                        } finally {
                            mainLock.unlock();
                        }

                        task.run();
                    } catch (InterruptedException e) {
                        //todo
                        throw new RuntimeException();
                    }
                }
            } catch (ShutdownException ex) {
                mainLock.lock();

                try {
                    threadpool.remove(thread);

                    //if it was the last thread, this ThreadPoolBlockingExecutor is completely shut down.
                    if (threadpool.isEmpty())
                        updateState(BlockingExecutorServiceState.Shutdown);
                } finally {
                    mainLock.unlock();
                }
            }
        }
    }

    class ShutdownException extends Exception {
    }
}
