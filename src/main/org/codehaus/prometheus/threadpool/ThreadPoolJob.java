/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

/**
 * The job a worker-thread in a {@link ThreadPool} executes. A ThreadPoolJob consists of 2 parts:
 * <ol>
 * <li>{@link #takeWork()} or {@link #takeWorkForNormalShutdown()}  getting task to execute</li>
 * <li>{@link #executeWork(Object)}: executing the task itself</li>
 * </ol>
 * <p/>
 * Examples:
 * The takeWork could be obtaining an item from a BlockingQueue (ThreadPoolBlockingExecutor} or
 * obtaining a reference from a LendableReference (ThreadPoolRepeater).
 * <p/>
 * A ThreadPoolJob can terminate the worker thread that executes by returning false in the
 * executeWork method (behavior while shutting down is undefined at the moment).
 * <p/>
 * The reason that the task is seperated in 2 parts is that the takeWork parts can be
 * interrupted if a threadpool needs to shutdown, or when idle threads needs to be removed
 * because the threadpool is shrinking.
 * <p/>
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public interface ThreadPoolJob<E> {

    /**
     * Takes a unit of work that is going to be executed by a ThreadPool worker. This call is made when
     * the ThreadPool is running normally. 
     *
     * This call should be responsive to interrupts, otherwise the Threadpool isn't able to interrupt
     * workers that are blocking on this call. And this prevents a timely shutdown of the ThreadPool.
     *
     * @return the data required for execution (value is not allowed to be null).
     * @throws InterruptedException if the calling thread was interrupted while waiting for work
     * @see #takeWorkForNormalShutdown()
     */
    E takeWork() throws InterruptedException;

    /**
     * Takes a unit of work that is going to be executed by a ThreadPool worker. This call is made when
     * the ThreadPool shuts down normally (so no forced shutdown).  To indicate that no work was found
     * for execution, null can be returned. 
     *
     * This call should not block indefinitely because this would lead to a ThreadPool that doesn't
     * shutdown.
     * 
     * @return the retrieved work, null indicates that no work is available anymore for execution.
     * @throws InterruptedException if the thread is interrupted while taking work.
     * @see #takeWork()
     */
    E takeWorkForNormalShutdown() throws InterruptedException;

    /**
     * Executes the work that was taken.
     *
     * todo:
     * what happens when false is returned, when work was received with the
     * {@link #takeWorkForNormalShutdown()}
     *
     * @param work the data required for execution. The value should never be null.
     * @throws Exception if something goes wrong while executing the work.
     * @return true if the Thread that executes this ThreadPoolJob should run again, false if it
     *              should terminate itself.
     * @see #takeWork ()
     * @see #takeWorkForNormalShutdown ()
     */
    boolean executeWork(E work) throws Exception;
}
