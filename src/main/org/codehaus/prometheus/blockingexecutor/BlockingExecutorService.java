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

    ExceptionHandler getExceptionHandler();
    
    void setExceptionHandler(ExceptionHandler handler);

    /**
     * if already started or paused nothing happens
     * if shutting down or shutdownNow -> illegalsstateexception
     */
    void start();

    /**
     * Shuts down this BlockingExecutor. All outstanding tasks
     * are executed, but new tasks are not accepted.
     * <p/>
     * If this BlockingExecutor already is shutting down,
     * or shutdownNow, this call is ignored.
     * <p/>
     * This call doesn't block until all outstanding and currently
     * started tasks are executed, a {@link #awaitShutdown()} has
     * to be used for that.
     *
     * todo: are started tasks interrupted?
     */
    void shutdown();

    /**
     * Shuts down this BlockingExecutor. All outstanding tasks
     * are not executed, but are returned. New tasks are not
     * accepted.
     * <p/>
     * If this BlockingExecutor already is shutting down, or shutdownNow,
     * this call is ignored.
     * <p/>
     * This call doesn't block until all currently started tasks are
     * executed, a {@link #awaitShutdown()} has to be used for that.
     *
     * todo: are started tasks interrupted?
     *
     * @return a List containing all not executed tasks.
     */
    List<Runnable> shutdownNow();

    /**
     * Returns the BlockingExecutorServiceState this BlockingExecutorService is in.
     *
     * @return the BlockingExecutorServiceState this BlockingExecutorService is in.
     */
    BlockingExecutorServiceState getState();

    /**
     * Waits until this service completely is shut down.
     * <p/>
     * This call can finish in 2 ways:
     * <ol>
     * <li>this BlockingExecutorService is completely shutdownNow</li>
     * <li>the calling thread is interrupted</li>
     * </ol>
     *
     * @throws InterruptedException if the current thread is interrupted while waiting for shutdownNow.
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
     * @throws TimeoutException
     * @throws InterruptedException
     * @throws NullPointerException if unit is null.
     */
    void tryAwaitShutdown(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException;
}
