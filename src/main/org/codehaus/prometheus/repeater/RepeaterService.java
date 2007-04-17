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
 * A {@link Repeater} that provides methods to access and change the state. If an implementation
 * needs more states, you can always create your own state.
 * <p/>
 * Pauzing will be added in one of the next releases.
 *
 * @author Peter Veentjer.
 */
public interface RepeaterService extends Repeater {



    /**
     * Starts this RepeaterService.
     * <p/>
     * If this RepeaterService already is started, nothing happens. If this RepeaterService is
     * shutting down, or already is shutdown, an IllegalStateException is thrown.
     *
     * @throws IllegalStateException if the RepeaterService is shutting down, or
     *                               already is shut down.
     */
    void start();



    void shutdown();

    /**
     * Stops this RepeaterService. If a task is running, it is interrupted. In one of the next
     * releases an extra method will be added for this.
     * <p/>
     * If this RepeaterService already is shuttingdown, or shutdown, the call is ignored. This
     * method can be called safely at every moment without throwing exceptions.
     * <p/>
     * This method doesn't block until this RepeaterService has shut down. See the
     * {@link #awaitShutdown()} for that. 
     *
     * @see #awaitShutdown()
     * @see #tryAwaitShutdown(long, java.util.concurrent.TimeUnit)   
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
     * @see #tryAwaitShutdown(long,java.util.concurrent.TimeUnit)
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
     * received.
     *
     * @return the state this RepeaterService is in.
     */
    RepeaterServiceState getState();

    /**
     *
     * @return
     */
    ExceptionHandler getExceptionHandler();

    /**
     * 
     * @param handler
     */
    void setExceptionHandler(ExceptionHandler handler);
}
