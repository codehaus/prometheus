/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

/**
 * <p/>
 * The ThreadPool is reponsible for managing a set of threads. Threads in threadpools are reused.
 * </p>
 * <p/>
 * The workers in the ThreadPool keep repeating a {@link ThreadPoolJob}.
 * For a worker to executes a WorkJob, it first needs to execute {@link ThreadPoolJob#takeWork()} method
 * and after it has got his task it calls the {@link ThreadPoolJob#executeWork(Object)}
 * method. The takeWork could be taking a reference from a LendableReference for example
 * (see {@link org.codehaus.prometheus.repeater.ThreadPoolRepeater}) or taking a task from a
 * blocking queue {@link org.codehaus.prometheus.blockingexecutor.ThreadPoolBlockingExecutor}. Workers
 * that are idle, are executing the takeWork method (they are waiting for work to execute) and else
 * they are working.
 * </p>
 * <p/>
 * <h1>Exception handling</h1>
 * <p/>
 * Exception handler: by injecting an instanceof of an {@link ExceptionHandler} one is able to
 * handle exceptions. Default a {@link org.codehaus.prometheus.exceptionhandler.NoOpExceptionHandler}
 * is used.
 * <p/>
 * Nothing is done with errors (they are not caught and this could lead to corrupted structures).
 * <p/>
 * <p/>
 * todo: maximum poolsize
 *
 * @author Peter Veentjer.
 * @see org.codehaus.prometheus.repeater.ThreadPoolRepeater
 * @see org.codehaus.prometheus.blockingexecutor.ThreadPoolBlockingExecutor
 * @see org.codehaus.prometheus.threadpool.ThreadPoolJob
 * @since 0.1
 */
public interface ThreadPool {

    Lock getStateChangeLock();

    /**
     * Starts this ThreadPool. If the ThreadPool already is running, the call is ignored.
     *
     * @throws IllegalStateException when the ThreadPool is shutting down, shutdown or
     *                               no default ThreadPoolJob is set.
     * @see #shutdownPolitly()
     */
    void start();

    /**
     * Shuts down this ThreadPool. It doesn't interrupt worker-threads while they are executing
     * a task (this could influence the time between the start of the shutdown, and the actual
     * shutdown).
     * <p/>
     * This method can be called safely whatever state the ThreadPool is in.
     * <p/>
     * This call doesn't wait for the shutdown to complete, see {@link #awaitShutdown()}
     *
     * @return the previous state the ThreadPool was in.
     * @see #shutdownNow()
     * @see #awaitShutdown()
     */
    ThreadPoolState shutdownPolitly();

    /**
     * Shuts down this ThreadPool immediately by interrupting workers while they are executing
     * a task. If the ThreadPool already is forced to shutdown, it is up to the implementation to
     * decide of worker-threads are interrupted again.
     * <p/>
     * This method can be called safely whatever state the ThreadPool is in.
     * <p/>
     * This call doesn't wait for the shutdown to complete, see {@link #awaitShutdown()}
     *
     * @return the previous state the ThreadPool was in.
     * @see #shutdownPolitly()
     * @see #awaitShutdown()
     */
    ThreadPoolState shutdownNow();

    /**
     * Awaits the shutdown of the ThreadPool but doesn't influence the state of the ThreadPool (so
     * it doesn't shutdown the ThreadPool). This call can be made safely no matter the state the
     * ThreadPool is in. If the ThreadPool already is shutdown, it returns immediately.
     *
     * @throws InterruptedException if the thread is interrupted while waiting for the complete shutdown
     *                              to take place.
     * @see #shutdownPolitly()
     * @see #tryAwaitShutdown(long,java.util.concurrent.TimeUnit)
     */
    void awaitShutdown() throws InterruptedException;

    /**
     * Awaits the shutdown of the ThreadPool with a timeout but doesn't influence the state of the ThreadPool (so
     * it doesn't shutdown the ThreadPool). This call can be made safely no matter the state the ThreadPool
     * is in. If the ThreadPool already is shutdown, it returns immediately.
     *
     * @param timeout how long to wait before giving up, in units of unit
     * @param unit    a TimeUnit determining how to interpret the timeout parameter
     * @throws InterruptedException if the thread is interrupted while waiting for the complete shutdown
     *                              to take place.
     * @throws TimeoutException     if a timeout occurred. todo:neg timeout
     * @throws NullPointerException if unit is <tt>null</tt>.
     * @see #shutdownPolitly()
     * @see #awaitShutdown()
     */
    void tryAwaitShutdown(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;

    /**
     * Spawns a ThreadPoolJob for execution.
     *
     * @param job
     * @throws NullPointerException if job is null
     * @throws IllegalStateException if the threadpool is not running
     */
    void spawn(ThreadPoolJob job);

    /**
     * Call is not atomic
     *
     * @param job
     * @param count
     */
    void spawn(ThreadPoolJob job, int count);

    /**
     * Registers initial spawns. This method doesn't start the ThreadPool if it hasn't
     * been started.
     * <p/>
     * If the job is not threadsafe, threadcount should be 1 to prevent other threads
     * from access the same job.
     *
     * @param job
     * @param threadcount the number of threads that are going to execute the job
     * @throws NullPointerException     if job is null
     * @throws IllegalArgumentException if threadcount smaller than 0
     * @throws IllegalStateException    if the ThreadPool is not in the unstarted or running
     *                                  state anymore.
     */
    void spawnWithoutStarting(ThreadPoolJob job, int threadcount);

    /**
     * Gets the ExceptionHandler this ThreadPool uses to handle exceptions. It will never be <tt>null</tt>.
     *
     * @return the ExceptionHandler of this ThreadPool.
     * @see #setExceptionHandler(ExceptionHandler)
     */
    ExceptionHandler getExceptionHandler();

    /**
     * Sets the ExceptionHandler this ThreadPool uses to handle exceptions.
     *
     * @param handler the new ExceptionHandler
     * @throws NullPointerException if handler is <tt>null</tt>.
     * @see #getExceptionHandler()
     */
    void setExceptionHandler(ExceptionHandler handler);

    /**
     * Returns the state this ThreadPool is in.
     *
     * @return the state this ThreadPool is in.
     */
    ThreadPoolState getState();

    /**
     * Returns the actual number of threads in this ThreadPool. The value could be stale when it is
     * received.
     *
     * @return the actual number of threads in the ThreadPool.
     */
    int getActualPoolSize();

    /**
     * Gets the desired poolsize. The desired poolsize doesn't have to match the actual poolsize.
     * It can take some time for the pool to grow or shrink to the desired poolsize. If the threadpool
     * is shutting down or shutdown, the returned value is undefined.
     *
     * @return the desired poolsize.
     * @see #getActualPoolSize()
     * @see #setDesiredPoolsize(int)
     */
    //int getDesiredPoolSize();

    /**
     * Sets the desired poolsize of this ThreadPool. The actual poolsize doesn't have to match
     * the desired poolsize because growing and shrinking of the pool can take some time. When
     * the threadpool already is shutting down, or shutdown, it is illegal to call this method.
     *
     * @param desiredPoolsize the desired size of the threadpool.
     * @throws IllegalArgumentException if desiredPoolsize is smaller than 0.
     * @throws IllegalStateException    if the ThreadPool is shutting down, or shutdown.
     * @see #getDesiredPoolSize()
     */
    //void setDesiredPoolsize(int desiredPoolsize);
}
