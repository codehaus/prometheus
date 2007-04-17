/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

/**
 * Utility functions for {@link Lock}.
 *
 * @author Peter Veentjer.
 */
public class LockUtil {

    /**
     * Acquires the lock if it is free within the given waiting time and the current thread has not
     * been interrupted.
     * <p/>
     * <p/>
     * This method calls {@link Lock#tryLock(long,TimeUnit)}.
     * <p/>
     * todo: negative timeouts
     *
     * @param lock    the Lock to acquire.
     * @param timeout how long to wait before giving up in units of <tt>unit</tt>.
     * @param unit    a <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt> parameter.
     * @return the remaining timeout, if the lock was not acquired, a negative value is returned.
     * @throws InterruptedException if the calling thread is interrupted.
     * @throws NullPointerException if lock or unit is <tt>null</tt>.
     */
    public static long tryLock(Lock lock, long timeout, TimeUnit unit) throws InterruptedException {
        return tryLockNanos(lock, unit.toNanos(timeout));
    }

    /**
     * todo: negative timeouts
     *
     * @param lock    the Lock to acquire.
     * @param timeout how long to wait before giving up in units of <tt>unit</tt>.
     * @param unit    a <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt> parameter.
     * @return the remaining timeout, this value will always be larger or equal to zero.
     * @throws InterruptedException if the current thread is interrupted
     * @throws TimeoutException     if the call has timed out.
     * @throws NullPointerException if lock or unit is <tt>null</tt>.
     */
    public static long tryLockProtected(Lock lock, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        long remainingNs = tryLock(lock, timeout, unit);
        ConcurrencyUtil.ensureNoTimeout(remainingNs);
        return remainingNs;
    }

    /**
     * todo: negative timeouts<p/>
     * todo: negative return values<p/>
     *
     * @param lock      the Lock to acquire.
     * @param timeoutNs how long to wait before giving up in nanoseconds.
     * @return the remaining tmeout
     * @throws InterruptedException if the current thread is interrupted
     * @throws TimeoutException     if the call has timed out
     * @throws NullPointerException if lock is <tt>null</tt>.
     */
    public static long tryLockNanosProtected(Lock lock, long timeoutNs) throws InterruptedException, TimeoutException {
        if (lock == null) throw new NullPointerException();

        timeoutNs = tryLockNanos(lock, timeoutNs);
        ConcurrencyUtil.ensureNoTimeout(timeoutNs);
        return timeoutNs;
    }

    /**
     * Acquires the lock if it is free within the given waiting time and the current thread has not
     * been {@link Thread#interrupt interrupted}.
     * <p/>
     * This method calls {@link Lock#tryLock(long,java.util.concurrent.TimeUnit)}, so see this
     * documentation for more information about the details of locking. The difference is that the
     * remaining timeout is returned instead of a boolean value.
     * <p/>
     * If this method is called with a negative value, no lock is acquired, no InterruptedException
     * is thrown and a negative value is returned.
     * <p/>
     * todo: what happens if this method is called with a nul timeout?<p/>
     * todo: should the timeout be decreased?<p/>
     *
     * @param lock      the Lock to acquire.
     * @param timeoutNs the timeout period in nanoseconds.
     * @return the remaining timeout in nanoseconds. If the lock was not acquired, a negative value
     *         is returned. A 0 or larger indicates that the lock was acquired.
     * @throws InterruptedException if the current thread is interrupted while acquiring the lock
     *                              (and interruption of lock acquisition is supported).
     * @throws NullPointerException if lock is <tt>null</tt>.
     */
    public static long tryLockNanos(Lock lock, long timeoutNs) throws InterruptedException {
        if (lock == null) throw new NullPointerException();

        if (timeoutNs < 0)
            return -1;

        long startTimeNs = System.nanoTime();
        boolean success = lock.tryLock(timeoutNs, TimeUnit.NANOSECONDS);
        if (!success) {
            return -1;//return -1 to signal the lock was not acquired.
        } else {
            long remainingNs = timeoutNs - (System.nanoTime() - startTimeNs);
            return remainingNs > 0 ? remainingNs : 0;
        }
    }


    //we don't want any instances.
    private LockUtil() {
    }
}
