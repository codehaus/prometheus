/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;


import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * A {@link Repeater} that provides methods to access and change the state.
 * <p/>
 * <h3>Exception handling</h3>
 * <p/>
 * Exceptions can be handled by injecting a {@link ExceptionHandler}. If no ExceptionHandler is injected,
 * caught exceptions are discarded.
 * <p/>
 * <h3>Shutting down</h3>
 * <p/>
 * A RepeaterService can be shutdown by calling:
 * </p>
 * <ol>
 * <li>
 * {@link #shutdown()}: doesn't interrupt a running task.
 * </li>
 * <li>
 * {@link #shutdownNow()}: does interrupt a running task.
 * </li>
 * </ol>
 * <p/>
 * Both shutdown methods can be called regardless of the state the RepeaterService is in. The call
 * doesn't await shutdown, see the {@link #awaitShutdown()} and {@link #tryAwaitShutdown(long,TimeUnit)}
 * methods.
 * </p>
 * <p/>
 * Pauzing/throttling will be added in one of the next releases.
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public interface RepeaterService extends Repeater {

    /**
     * Starts this RepeaterService.
     * <p/>
     * If this RepeaterService already is running, nothing happens. If this RepeaterService is
     * shutting down, or already is shutdown, an IllegalStateException is thrown.
     *
     * @throws IllegalStateException if the RepeaterService is shutting down, or
     *                               already is shut down.
     */
    void start();

    /**
     * Shuts down this RepeaterService. If a task is running, it is not interrupted. If you want
     * to interrupt the running task (to force shutdown), see {@link #shutdownNow()}.
     * <p/>
     * If this RepeaterService already is shuttingdown, or shutdown, the call is ignored. This
     * method can be called safely at every moment without throwing exceptions.
     * <p/>
     * This method doesn't block until this RepeaterService has shut down. See the
     * {@link #awaitShutdown()} for that.
     *
     * @see #shutdown()
     * @see #awaitShutdown()
     * @see #tryAwaitShutdown(long,TimeUnit)
     */
    void shutdown();

    /**
     * Shuts down this RepeaterService. If a task is running, it is interrupted. If you don't want
     * to interrupt the running task, see {@link #shutdown()}.
     * <p/>
     * If this RepeaterService already is shuttingdown, the tasks are interrupted. If this repeater
     * is shutdown, the call is ignored. This method can be called safely at every moment without
     * throwing exceptions.
     * <p/>
     * This method doesn't block until this RepeaterService has shut down. See the
     * {@link #awaitShutdown()} for that.
     *
     * @see #shutdown()
     * @see #awaitShutdown()
     * @see #tryAwaitShutdown(long,TimeUnit)
     */
    void shutdownNow();

    /**
     * Waits for the termination of this RepeaterService. The state the RepeaterService is in
     * doesn't matter, so this call always can be made safely. This call completes a soon as:
     * <ol>
     * <li>the RepeaterService is completely stopped,</li>
     * <li>the thread is interrupted while waiting./li>
     * </ol>
     *
     * @throws InterruptedException if the thread is interrupted while waiting for termination.
     * @see #tryAwaitShutdown(long,TimeUnit)
     * @see #shutdownNow()
     */
    void awaitShutdown() throws InterruptedException;

    /**
     * Waits for the termination of this RepeaterService with a timeout. The state the
     * RepeaterService is in doesn't matter, so this call always can be made safely. This call
     * completes a soon as:
     * <ol>
     * <li>the RepeaterService is completely stopped,</li>
     * <li>the thread is interrupted while waiting</li>
     * <li>a timeout occurs</li>
     * </ol>
     *
     * @param timeout how long to wait before giving up in units of <tt>unit</tt>.
     * @param unit    a <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt> parameter.
     * @throws NullPointerException if unit is <tt>null</tt>.
     * @throws TimeoutException     if a timeout has occurred.
     * @throws InterruptedException if the thread is interrupted while waiting for termination.
     * @see #awaitShutdown()
     * @see #shutdownNow()
     */
    void tryAwaitShutdown(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException;

    /**
     * Returns the state this RepeaterService is in. This value could be stale as soon as it is
     * returned.
     *
     * @return the state this RepeaterService is in.
     */
    RepeaterServiceState getState();

    /**
     * Gets the current ExceptionHandler. The value will never be <tt>null</tt>. The value could
     * be stale as soon as it is returned.
     *
     * @return the current ExceptionHandler.
     */
    ExceptionHandler getExceptionHandler();

    /**
     * Sets the ExceptionHandler this RepeaterService uses to handle exceptions. This method can
     * be called regardless of the state the RepeaterService is in.
     *
     * @param handler the ExceptionHandler this RepeaterService uses to handle exceptions.
     * @throws NullPointerException if handler is null.
     */
    void setExceptionHandler(ExceptionHandler handler);
}
