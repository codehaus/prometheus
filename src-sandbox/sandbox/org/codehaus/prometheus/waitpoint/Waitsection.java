/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.waitpoint;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This interface needs to be unified with the Waitpoint?
 * <p/>
 * A critical interruptiblesection could be created by using a waitsection
 * and a mutex of binary semaphore for example.
 * <p/>
 * <p/>
 * The Waitsection looks a lot like the LendableReference,
 * the only thing that is different is that no value is returned.
 * The same comparison can be made between the Waitpoint and the
 * AwaitableReference.
 * <p/>
 * In java 7 a closure could be added to make it more attractive to use.
 *
 * @author Peter Veentjer.
 */
public interface Waitsection {

    /**
     * Enters this Waitsection. If this waitsection can't be entered,
     * this call blocks until:
     * <ol>
     * <li>the waitsection can be entered</li>
     * <li>the thread is interrupted</li>
     * </ol>
     * If the interruptstatus is set, an InterruptedException doesn't
     * need to be thrown; it is up to the implementation to decide.
     *
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    void enter() throws InterruptedException;

    /**
     * Enters this Waitsection. This call can't be interrupted.
     * <p/>
     * If the interrupt flag is set while entering this method, the flag
     * remains set.
     */
    void enterUninterruptibly();

    /**
     * Checks if this Waitsection can be entered. The value could be stale
     * as soon as it is returned.
     * <p/>
     * If the interrupt flag is set when entering this method, it remains set.
     *
     * @return true if this Waitsection can be entered, false otherwise.
     */
    boolean isEnterable();

    /**
     * Tries to enter this Waitsection. If the Waitsection can't be entered,
     * this call returns immediately.
     * <p/>
     * If the interrupt flag is set when entering this method, it remains set.
     *
     * @return true if the enter was successful, false otherwise.
     */
    boolean tryEnter();

    /**
     * Tries to enter this Waitsection with a timeout.
     *
     * @param timeout
     * @param unit
     * @return the remaining timeout. This will always be a value greater or equal to zero.
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws NullPointerException if unit is null.
     */
    long tryEnter(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;

    /**
     * Tries to enter this Waitsection with a timeout.
     * <p/>
     * If the interruptstatus is set when this method is called, it is
     * not changed.
     *
     * @param timeout
     * @param unit
     * @return the remaining timeout. This will always be a value greater or equal to zero.
     * @throws TimeoutException
     * @throws NullPointerException if unit is null.
     */
    long tryEnterUninterruptibly(long timeout, TimeUnit unit) throws TimeoutException;

    /**
     * Exits this EnteringWaitpoint.
     * <p/>
     * It depends on the implementation of the Waitsection what type
     * of exception is thrown.
     * <p/>
     * todo: what happens if the thread hasn't entered the waitpoint.
     * <p/>
     * todo: what happens when a different thread calls exit.
     */
    void exit();
}
