/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

/**
 * The job a worker-thread in a {@link ThreadPool} executes by implements the logic of
 * one iteration in the worker execution loop. A ThreadPoolJob consists of 2 parts:
 * <ol>
 * <li>{@link #takeWork()}: taking work to execute</li>
 * <li>{@link #executeWork(Object)}: executing the work received by {@link #takeWork()}</li>
 * </ol>
 * The reason why this functionality is split in 2 parts, is that the takeWork parts needs to be
 * interruptible; if a threadpool needs to shutdown, or when idle threads needs to be removed
 * because the threadpool is shrinking. 
 * <p/>
 * Examples:
 * The takeWork could be obtaining an item from a BlockingQueue (ThreadPoolBlockingExecutor} or
 * obtaining a reference from a LendableReference (ThreadPoolRepeater).
 * <p/>
 * A ThreadPoolJob can terminate the worker thread that executes by returning false in the
 * executeWork method (behavior while shutting down is undefined at the moment).
 * <p/>
 * 
 * @author Peter Veentjer.
 * @since 0.1
 */
public interface ThreadPoolJob<E> {

    /**
     * Takes a unit of work that is going to be executed by a ThreadPool worker.
     *
     * This call should be responsive to interrupts, otherwise the Threadpool isn't able to interrupt
     * workers that are blocking on this call. And this prevents a timely shutdown of the ThreadPool.
     *
     * @return the taken work required for execution (value is not allowed to be null).
     * @throws InterruptedException if the calling thread was interrupted while waiting for work
     */
    E takeWork() throws InterruptedException;

    /**
     * Executes the work that was taken.
     *
     * @param work the data required for execution. The value should never be null.
     * @throws Exception if something goes wrong while executing the work.
     * @return true if the Thread that executes this ThreadPoolJob should run again, false if it
     *              should terminate itself.
     * @see #takeWork ()
     */
    boolean executeWork(E work) throws Exception;
}
