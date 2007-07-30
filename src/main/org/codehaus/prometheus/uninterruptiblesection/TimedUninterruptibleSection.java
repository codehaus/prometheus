/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.uninterruptiblesection;

import org.codehaus.prometheus.util.ConcurrencyUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * A timed version of the {@link UninterruptibleSection}.
 *
 * @author Peter Veentjer.
 * @see UninterruptibleSection
 * @since 0.1
 */
public abstract class TimedUninterruptibleSection<E> {

    /**
     * The original section that contains the interruptible calls and is protected by this
     * TimedUninterruptibleSection.
     *
     * @param timeoutNs how long to wait before giving up in nanoseconds. This value will always
     *                  be equal or larger than zero.
     * @return the result of the section
     * @throws InterruptedException if the calling thread is interrupted while executing the section.
     * @throws TimeoutException     if a timeout occurs
     */
    protected abstract E originalsection(long timeoutNs) throws InterruptedException, TimeoutException;

    /**
     * Tries to execute the original section.
     * <p/>
     * Calling with a negative timeout will always lead to a TimeoutException.
     * <p/>
     * todo:explain 0 timeout.
     *
     * @param timeout how long to wait before giving up in units of <tt>unit</tt>
     * @param unit    a <tt>TimeUnit</tt> determining how to interpret the
     *                <tt>timeout</tt> parameter.
     * @return the value returned from the interruptible section
     * @throws TimeoutException     if the call times out.
     * @throws NullPointerException if unit is <tt>null</tt>.
     */
    public final E tryExecute(long timeout, TimeUnit unit) throws TimeoutException {
        long timeoutNs = ConcurrencyUtil.toUsableNanos(timeout, unit);

        boolean restoreInterrupt = Thread.interrupted();
        try {
            while (true) {
                long startNs = System.nanoTime();
                try {
                    return originalsection(timeoutNs);
                } catch (InterruptedException e) {
                    restoreInterrupt = true;

                    timeoutNs -= System.nanoTime() - startNs;
                    if (timeoutNs <= 0)
                        throw new TimeoutException();
                }
            }
        } finally {
            if (restoreInterrupt)
                Thread.currentThread().interrupt();
        }
    }

    /**
     * Tries to execute the uninterruptible section with a 0 timeout.
     * A TimeoutException needs to be thrown to indicate a timeout occurred because it
     * can't be expressed in the return value. A null as return value is valid for
     * {@link #tryExecute(long,java.util.concurrent.TimeUnit)}
     *
     * @return the value returned from the interruptible section.
     * @throws TimeoutException if the call times out.
     */
    public final E tryExecute() throws TimeoutException {
        return tryExecute(0, TimeUnit.NANOSECONDS);
    }
}
