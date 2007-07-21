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
 * Extends the {@link BlockingExecutor} and adds some control features like shutting
 * down and starting.
 *
 * @author Peter Veentjer.
 */
public interface BlockingExecutorService extends BlockingExecutor {

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

    /**
     * Starts this BlockingExecutorService. If the BlockingExecutorService already is running,
     * the call is ignored.
     *
     * @throws IllegalStateException if the BlockingExecutorService already is shutting down, or
     *                               is shutdown.
     */
    void start();

    /**
     * Shuts down this BlockingExecutor. All outstanding tasks are executed, but new tasks are not
     * accepted. Running tasks and outstanding tasks are not interrupted.
     * <p/>
     * If this BlockingExecutor already is shutting down or shutdown, this call is ignored.
     * <p/>
     * This call doesn't block until all outstanding and currently running tasks are executed,
     * a {@link #awaitShutdown()} has to be used for that.
     */
    void shutdown();

    /**
     * Shuts down this BlockingExecutor. Outstanding tasks are not executed, but are returned. New tasks are
     * not accepted. Running tasks are interrupted, it is up to the task to be responsive to interrupts. If
     * a task isn't responsive, it can lead to a delay in the shutdown.
     * <p/>
     * If this BlockingExecutor already is shutting down or shutdown, this call is ignored.
     * <p/>
     * This call doesn't block until all currently running tasks are executed, a
     * {@link #awaitShutdown()} has to be used for that.
     *
     * @return a List containing all outstanding tasks.
     */
    List<Runnable> shutdownNow();

    /**
     * Returns the state this BlockingExecutorService is in.
     *
     * @return the state this BlockingExecutorService is in.
     */
    BlockingExecutorServiceState getState();

    /**
     * Waits until this service completely is shut down.
     * <p/>
     * This call can finish in 2 ways:
     * <ol>
     * <li>this BlockingExecutorService is completely shutdown</li>
     * <li>the calling thread is interrupted and throws an InterruptedException</li>
     * </ol>
     *
     * @throws InterruptedException if the current thread is interrupted while waiting for shutdown.
     */
    void awaitShutdown() throws InterruptedException;

    /**
     * Waits until this service compelely is shut down.
     * <p/>
     * This call can finish in 3 ways:
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
}
