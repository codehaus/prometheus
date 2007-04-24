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
 * The BlockingExecutorService extends the {@link BlockingExecutor} and
 * adds some control features like stopping and starting.
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
     *
     * @param handler
     */
    void setExceptionHandler(ExceptionHandler handler);

    /**
     * Starts this BlockingExecutorService.
     *
     * if already started or paused nothing happens
     * if shutting down or shutdownNow -> illegalsstateexception
     */
    void start();

    /**
     * Shuts down this BlockingExecutor. All outstanding tasks are executed, but new tasks are not
     * accepted. Running tasks are not interrupted.
     * <p/>
     * If this BlockingExecutor already is shutting down or shutdownNow, this call is ignored.
     * <p/>
     * This call doesn't block until all outstanding and currently started tasks are executed,
     * a {@link #awaitShutdown()} has to be used for that.
     */
    void shutdown();

    /**
     * Shuts down this BlockingExecutor. All outstanding tasks are not executed, but are returned.
     * New tasks are not accepted. Running tasks are interrupted, it is up to the task to be responsive
     * to interrupts. If a task isn't responsibe, it can lead to a delay in the shutdown.
     * <p/>
     * If this BlockingExecutor already is shutting down, or shutdownNow,
     * this call is ignored.
     * <p/>
     * This call doesn't block until all currently started tasks are
     * executed, a {@link #awaitShutdown()} has to be used for that.
     *
     * @return a List containing all not executed tasks.
     */
    List<Runnable> shutdownNow();

    /**
     * Returns the state this BlockingExecutorService has.
     *
     * @return the state this BlockingExecutorService has.
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
     * Waits until this service compelely is shutdownNow.
     * <p/>
     * This call can finish in 3 ways:
     * <ol>
     * <li>this BlockingExecutorService is completely shutdownNow</li>
     * <li>a timeout occurs</li>
     * <li>the calling thread is interrupted</li>
     * </ol>
     *
     * @param timeout
     * @param unit
     * @throws TimeoutException if the wait has timed out.
     * @throws InterruptedException if the wait was interrupted
     * @throws NullPointerException if unit is null.
     */
    void tryAwaitShutdown(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException;
}
