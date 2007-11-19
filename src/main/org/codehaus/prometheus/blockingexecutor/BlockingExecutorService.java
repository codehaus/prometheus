/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Extends the {@link BlockingExecutor} and adds life cycle control functions like starting, shutting
 * down and exception handling.
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public interface BlockingExecutorService extends BlockingExecutor {

    /**
     * Returns the state this BlockingExecutorService is in.
     *
     * @return the state this BlockingExecutorService is in.
     */
    BlockingExecutorServiceState getState();

    /**
     * Starts this BlockingExecutorService. If the BlockingExecutorService already is running,
     * the call is ignored.
     *
     * @throws IllegalStateException if the BlockingExecutorService already is shutting down, or
     *                               is shutdown.
     */
    void start();

    /**
     * Shuts down this BlockingExecutor  politly. All outstanding tasks are executed, but new tasks are not
     * accepted. Running tasks and outstanding tasks are not interrupted.
     * <p/>
     * If this BlockingExecutor already is shutting down or shutdown, this call is ignored.
     * <p/>
     * This call doesn't block until complete shutdown, see {@link #awaitShutdown()} for that.
     *
     * @return a list of unprocessed jobs. This list only contains job if the threadpool is empty.
     *         Otherwise all tasks are processed.
     */
    List<Runnable> shutdownPolitly();

    /**
     * Shuts down this BlockingExecutor. All outstanding tasks are not executed (the workqueue is drained)
     * but the current running tasks are not interrupted.
     *
     * @return a List of unprocessed jobs
     */
    List<Runnable> shutdownAndDrain();

    /**
     * Shuts down this BlockingExecutor. Outstanding tasks are not executed, but are returned. New tasks are
     * not accepted. Running tasks are interrupted, it is up to the task to be responsive to interrupts. If
     * a task isn't responsive, it can lead to a delay in the shutdown.
     * <p/>
     * If this BlockingExecutor already is shutting down or shutdown, this call is ignored.
     * <p/>
     * This call doesn't block until complete shutdown, see {@link #awaitShutdown()} for that.  *
     *
     * @return a List of unprocessed jobs
     */
    List<Runnable> shutdownNow();

    /**
     * Waits till this BlockingExecutorService has shutdown completely. This call can finish in 2 ways:
     * <ol>
     * <li>this BlockingExecutorService is completely shutdown</li>
     * <li>the calling thread is interrupted and throws an InterruptedException</li>
     * </ol>
     *
     * @throws InterruptedException if the current thread is interrupted while waiting for shutdown.
     */
    void awaitShutdown() throws InterruptedException;

    /**
     * Tries to waits till this BlockingExecutorService has shutdown completely. This call can finish in
     * 3 ways:
     * <ol>
     * <li>this BlockingExecutorService is completely shutdown</li>
     * <li>a timeout occurs</li>
     * <li>the calling thread is interrupted</li>
     * </ol>
     *
     * @param timeout how long to wait before giving up in units of <tt>unit</tt>.
     * @param unit    a <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt>
     *                parameter.
     * @throws TimeoutException     if the wait has timed out.
     * @throws InterruptedException if the wait was interrupted
     * @throws NullPointerException if unit is null.
     */
    void tryAwaitShutdown(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException;

    /**
     * Returns the ExceptionHandler this BlockingExecutorService uses to handle exceptions.
     * The returned value will never be <tt>null</tt>.
     *
     * @return the ExceptionHandler this BlockingExecutorService uses to handle exceptions.
     */
    ExceptionHandler getExceptionHandler();

    /**
     * Sets the ExceptionHandler this BlockingExecutorService uses to handle exceptions.
     *
     * @param handler the new handler.
     * @throws NullPointerException if handler is null.
     */
    void setExceptionHandler(ExceptionHandler handler);
}
