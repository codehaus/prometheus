/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p/>
 * The ThreadPool is reponsible for managing a set of threads. Threads in threadpools are reused.
 * </p>
 * <p/>
 * The workers in the ThreadPool keep repeating a {@link ThreadPoolJob}.
 * For a worker to executes a WorkJob, it first needs to execute {@link ThreadPoolJob#getWork()} method
 * and after it has got his task it calls the {@link ThreadPoolJob#executeWork(Object)}
 * method. The getWork could be taking a reference from a LendableReference for example
 * (see {@link org.codehaus.prometheus.repeater.ThreadPoolRepeater}) or taking a task from a
 * blocking queue {@link org.codehaus.prometheus.blockingexecutor.ThreadPoolBlockingExecutor}. Workers
 * that are idle, are executing the getWork method (they are waiting for work to execute) and else
 * they are working.
 * </p>
 * <p/>
 * When a ThreadPool is shutdown, it starts calling the {@link ThreadPoolJob#getShuttingdownWork()}.
 * When a ThreadPool is forced to shutdown by calling the {@link #shutdownNow()} method, no methods
 * are called on the ThreadPoolJob anymore and the pooled thread is able to rest in peace.
 * </p>
 * <h1>Exception handling</h1>
 * <p/>
 * Exception handler: by injecting an instanceof of an {@link ExceptionHandler} one is able to
 * handle exceptions. Default a {@link org.codehaus.prometheus.exceptionhandler.NullExceptionHandler}
 * is used.
 * <p/>
 * Nothing is done with errors (they are not caught and this could lead to corrupted structures).
 * <p/>
 *
 * @author Peter Veentjer.
 * @see org.codehaus.prometheus.repeater.ThreadPoolRepeater
 * @see org.codehaus.prometheus.blockingexecutor.ThreadPoolBlockingExecutor
 * @see org.codehaus.prometheus.threadpool.ThreadPoolJob
 * @since 0.1
 */
public interface ThreadPool {

    /**
     * Starts this ThreadPool. If the ThreadPool already is running, the call is ignored.
     *
     * @throws IllegalStateException when the ThreadPool is shutting down, shutdown or
     *                               no default ThreadPoolJob is set.
     * @see #shutdown()
     */
    void start();

    /**
     * Shuts down this ThreadPool. It doesn't interrupt worker-threads while they are executing
     * a task.
     * <p/>
     * This method can be called safely whatever state the ThreadPool is in.
     * <p/>
     * This call doesn't wait for the shutdown to complete, see {@link #awaitShutdown()}
     *
     * @return the previous state the ThreadPool was in.
     * @see #shutdownNow()
     * @see #awaitShutdown()
     */
    ThreadPoolState shutdown();

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
     * @see #shutdown()
     * @see #awaitShutdown()
     */
    ThreadPoolState shutdownNow();

    /**
     * Awaits the shutdown of the ThreadPool but doesn't influence the state of the ThreadPool. This
     * call can be made safely no matter the state the ThreadPool is in. If the ThreadPool already is
     * shutdown, it returned immediately.
     *
     * @throws InterruptedException if the thread is interrupted while waiting for the complete shutdown
     *                              to take place.
     * @see #shutdown()
     * @see #tryAwaitShutdown(long,java.util.concurrent.TimeUnit)
     */
    void awaitShutdown() throws InterruptedException;

    /**
     * Awaits the shutdown of the ThreadPool but doesn't influence the state of the ThreadPool. This
     * call can be made safely no matter the state the ThreadPool is in. If the ThreadPool already is
     * shutdown, it returned immediately.
     *
     * @param timeout how long to wait before giving up, in units of unit
     * @param unit    a TimeUnit determining how to interpret the timeout parameter
     * @throws InterruptedException if the thread is interrupted while waiting for the complete shutdown
     *                              to take place.
     * @throws TimeoutException     if a timeout occurred. todo:neg timeout
     * @throws NullPointerException if unit is <tt>null</tt>.
     * @see #shutdown()
     * @see #awaitShutdown()
     */
    void tryAwaitShutdown(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;


    /**
     * Gets the job this ThreadPool is executing.  If no job is set, <tt>null</tt> is returned.
     *
     * @return the job this ThreadPool is executing.
     * @see #setJob(ThreadPoolJob)
     */
    ThreadPoolJob getWorkerJob();

    /**
     * Sets the ThreadPoolJob this ThreadPool executes.
     *
     * @param job the
     * @throws NullPointerException  if job is <tt>null</tt>.
     * @throws IllegalStateException if the ThreadPool isn't in the unstarted state anymore.
     * @see #setJob(ThreadPoolJob)
     */
    void setJob(ThreadPoolJob job);

    /**
     * Gets the ExceptionHandler. The value will never be <tt>null</tt>.
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
     * @see #getDesiredPoolSize()
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
    int getDesiredPoolSize();

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
    void setDesiredPoolsize(int desiredPoolsize);
}
