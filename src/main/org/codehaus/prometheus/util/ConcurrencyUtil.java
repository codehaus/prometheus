/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The ConcurrencyUtil contains various concurrency related convenience functions.
 *
 * @author Peter Veentjer.
 */
public class ConcurrencyUtil {

    //number of nano seconds per millisecond.
    private static final long NANOS_PER_MILLI = TimeUnit.MILLISECONDS.toNanos(1);

    /**
     * Checks if a timeout occurred. A timeout occurrs when the timeout is smaller than 0. It
     * doesn't matter in which TimeUnit timeout is expressed.
     * <p/>
     * This method should not be used to check awaits of Conditions because a 0 with a Condition
     * indicates a timeout and 0 with this method doesn't indicate a timeout (this is the same
     * behavior as with a {@link java.util.concurrent.locks.Lock}).
     *
     * @param timeout the timeout in nanoseconds.
     * @return <tt>true</tt> if a timeout has occurred, <tt>false</tt> otherwise.
     */
    public static boolean hasTimeoutOccurred(long timeout) {
        return timeout < 0;
    }

    /**
     * Makes sure no timeout occurs. If a timeout has occurred (the timeout is smaller than 0) a
     * TimeoutException is thrown.
     * <p/>
     * This method should not be used to check awaits of Conditions because a 0 with a Condition
     * indicates a timeout and 0 with this method doesn't indicate a timeout (this is the same
     * behavior as with a {@link java.util.concurrent.locks.Lock}).
     *
     * @param timeout the timeout. It doesn't matter in which TimeUnit timeout is expressed.
     * @throws TimeoutException if timeout is smaller than zero
     */
    public static void ensureNoTimeout(long timeout) throws TimeoutException {
        if (hasTimeoutOccurred(timeout))
            throw new TimeoutException();
    }

    /**
     * Converts a timeout and a unit to a non negative timeout in nanoseconds.
     *
     * @param timeout the timeout
     * @param unit    a <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt>
     *                parameter.
     * @return the timeout in nanoseconds. This will always be a value greater or equal to zero.
     * @throws TimeoutException     if the timeout is smaller than 0.
     * @throws NullPointerException if unit is <tt>null</tt>.
     */
    public static long toUsableNanos(long timeout, TimeUnit unit) throws TimeoutException {
        if (unit == null) throw new NullPointerException();

        long ns = unit.toNanos(timeout);
        ensureNoTimeout(ns);
        return ns;
    }

    /**
     * Lets the current Thread sleepUninterruptibly for a certain period. If the period is equal or
     * smaller than zero, no sleeping is done. This call can't be interrupted.
     * <p/>
     * This method makes use of {@link Thread#sleep(long,int)}. If a call to this method is
     * interrupted, a new sleepMsOld is tried with the remaining timeout.
     * <p/>
     * todo:
     * remark about calling with interruptstatus set.
     *
     * @param period
     * @param unit   a <tt>TimeUnit</tt> determining how to interpret the <tt>period</tt> parameter.
     * @throws RuntimeException     if this thread is interrupted while sleeping.
     * @throws NullPointerException if unit is null.
     * @see #sleepUninterruptibly(long,java.util.concurrent.TimeUnit)
     * @see Thread#sleep(long,int)
     */
    @SuppressWarnings({"UnnecessaryLocalVariable"})
    public static void sleepUninterruptibly(long period, TimeUnit unit) {
        if (unit == null) throw new NullPointerException();

        if (period <= 0)
            return;

        //todo: can't this be done with an uninterruptiblesection?

        long remainingPeriodNs = unit.toNanos(period);
        boolean interrupted = Thread.interrupted();
        try {
            while (remainingPeriodNs > 0) {
                long startNs = System.nanoTime();
                try {
                    long ms = TimeUnit.NANOSECONDS.toMillis(remainingPeriodNs);
                    int ns = (int) (remainingPeriodNs % TimeUnit.MILLISECONDS.toNanos(1));

                    Thread.sleep(ms, ns);
                    remainingPeriodNs = 0;
                } catch (InterruptedException e) {
                    interrupted = true;
                    long elapsedNs = System.nanoTime() - startNs;
                    remainingPeriodNs -= elapsedNs;
                }
            }
        } finally {
            if (interrupted)
                Thread.currentThread().interrupt();
        }
    }

    /**
     * Lets the calling thread sleepMsOld for a certain period. If the period is equal or smaller than
     * zero, no sleeping is done. This call can be interrupted.
     * <p/>
     * This method makes use of {@link Thread#sleep(long,int)}. At the moment there is no giveOthersAChance
     * method in the JRE that accepts a period with a unit.
     *
     * @param period the period to giveOthersAChance
     * @param unit   a <tt>TimeUnit</tt> determining how to interpret the <tt>period</tt> parameter.
     * @throws InterruptedException if the calling thread is interrupted while sleeping.
     * @throws NullPointerException if unit is <tt>null</tt>.
     * @see #sleep(long,java.util.concurrent.TimeUnit)
     * @see Thread#sleep(long,int)
     */
    public static void sleep(long period, TimeUnit unit) throws InterruptedException {
        if (unit == null) throw new NullPointerException();

        if (period <= 0)
            return;

        long ms = unit.toMillis(period);
        int ns = (int) (unit.toNanos(period) % NANOS_PER_MILLI);
        Thread.sleep(ms, ns);
    }


    //we don't want any instances.
    private ConcurrencyUtil() {
    }


}
