/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import org.codehaus.prometheus.uninterruptiblesection.TimedUninterruptibleSection;
import static org.codehaus.prometheus.util.ConcurrencyUtil.ensureNoTimeout;
import static org.codehaus.prometheus.util.ConcurrencyUtil.toUsableNanos;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;

/**
 * Utility functions for {@link Condition}.
 * <p/>
 * When an {@link Condition#awaitNanos(long)}  returns 0 or smaller, the await was not successful
 * (timeout occurred).
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public class ConditionUtil {

    /**
     * Waits for an event on a condition to occur. This function calls {@link Condition#awaitNanos(long)} but
     * throws a TimeoutException instead of returning a value equal or smaller to zero to indicate that a timeout
     * has occurred. This means that the returned value always will be larger than zero.
     * <p/>
     * If the thread is now the owner of the condition (doesn't have the corresponding lock) it
     * could happen that a IllegalThreadMonitorException is thrown. This depends on the
     * implementation of the lock.
     * <p/>
     * If a negative timeout is passed, a TimeoutException is thrown (even when the thread his
     * interrupt flag is activated). Is this consistent?
     * todo: nul timeouts
     *
     * @param condition the condition to wait on.
     * @param timeoutNs how long before giving up in nanoseconds.
     * @return the remaining timeout in nanoseconds. This will always be a value larger than zero.
     * @throws NullPointerException if condition is <tt>null</tt>.
     * @throws InterruptedException if the thread is interrupted while waiting.
     * @throws TimeoutException     if a timeout occurs.
     * @see Condition#awaitNanos(long)
     */
    public static long awaitNanosOrThrow(Condition condition, long timeoutNs)
            throws InterruptedException, TimeoutException {
        if (condition == null) throw new NullPointerException();

        ensureNoTimeout(timeoutNs);
        long remainingNs = condition.awaitNanos(timeoutNs);
        ensureNoAwaitTimeout(remainingNs);
        return remainingNs;
    }

    /**
     * @param condition the Condition to wait on.
     * @param timeout   how long to wait before giving up in units of <tt>unit</tt>.
     * @param unit      a <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt>
     *                  parameter.
     * @return the remaining timeout (always larger than 0)
     * @throws InterruptedException if the thread is interrupted while waiting.
     * @throws NullPointerException if condition or unit is <tt>null</tt>.
     * @throws TimeoutException     if a timeout occurs.
     * @see #awaitNanosUninterruptiblyOrThrow(Condition,long)
     */
    public static long awaitOrThrow(Condition condition, long timeout, TimeUnit unit)
            throws InterruptedException, TimeoutException {
        if (condition == null || unit == null) throw new NullPointerException();
        return awaitNanosOrThrow(condition, unit.toNanos(timeout));
    }

    /**
     * Waits on a condition uninterruptibly.  The reason why this method exists, is that the
     * {@link Condition} has no timed uninterruptible wait.
     * <p/>
     * todo: null timeout
     *
     * @param condition the condition to wait on
     * @param timeoutNs the timeout period in nanoseconds
     * @return the remaining timeout (always larger than 0)
     * @throws TimeoutException     when a timeout occurrs
     * @throws NullPointerException if condition is null.
     * @see java.util.concurrent.locks.Condition#awaitUninterruptibly()
     */
    public static long awaitNanosUninterruptiblyOrThrow(final Condition condition, long timeoutNs)
            throws TimeoutException {
        if (condition == null) throw new NullPointerException();

        TimedUninterruptibleSection<Long> section = new TimedUninterruptibleSection<Long>() {
            protected Long interruptibleSection(long timeoutNs)
                    throws InterruptedException, TimeoutException {
                timeoutNs = condition.awaitNanos(timeoutNs);
                ensureNoAwaitTimeout(timeoutNs);
                return timeoutNs;
            }
        };

        return section.tryExecute(timeoutNs, TimeUnit.NANOSECONDS);
    }

    /**
     * Waits on a Condition uninterruptible.
     *
     * @param condition the condition to wait on
     * @param timeout   the timeout period in nanoseconds
     * @param unit      a <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt>
     *                  parameter.
     * @return the remaining timeout (always larger than 0)
     * @throws TimeoutException     when a timeout occurrs
     * @throws NullPointerException if condition or unit is null.
     * @see #awaitNanosUninterruptiblyOrThrow(java.util.concurrent.locks.Condition, long)
     */
    public static long awaitUninterruptiblyOrThrow(Condition condition, long timeout, TimeUnit unit)
            throws TimeoutException {
        return awaitNanosUninterruptiblyOrThrow(condition, toUsableNanos(timeout, unit));
    }

    /**
     * Makes sure that no timeout has occurred while doing an await on a Condition. A 0
     * indicates that a timeout has occurred.
     *
     * @param remainingNs the remaining number of nanoseconds.
     * @throws TimeoutException if timeout is smaller or equal to zero.
     */
    public static void ensureNoAwaitTimeout(long remainingNs) throws TimeoutException {
        if (remainingNs <= 0)
            throw new TimeoutException();
    }

    //we don't want any instances.
    private ConditionUtil() {
    }
}
