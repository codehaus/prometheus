/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import static org.codehaus.prometheus.util.ConcurrencyUtil.toUsableNanos;
import static org.codehaus.prometheus.util.ConditionUtil.awaitNanosAndThrow;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A Latch is a single shot waiting mechanism. Once it has been opened, it can't be closed again.
 * <p/>
 * idea: also a Latch(fair) constructor?<p/>
 * todo: explain fairness with mainlock<p/>
 * todo: explain why the closeable interface is not implemented<p/>
 * todo: explain why the waitpoint interface is not implemented<p/>
 * todo: explain why the AbstractQueuedSynhronizer was not used<p/>
 * <p/>
 * Doug Lea his library also contained a
 * <a href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/Latch.java">Latch"> but
 * it never made it in the java.util.concurrent package.
 *
 * @author Peter Veentjer.
 */
public class Latch {

    public static Lock newDefaultMainLock() {
        return new ReentrantLock();
    }

    private final Lock mainLock;
    private final Condition isOpenCondition;
    private volatile boolean open = false;

    /**
     * Creates a new unfair Latch with a {@link ReentrantLock#ReentrantLock()}
     * as main lock.
     */
    public Latch() {
        this(newDefaultMainLock());
    }

    /**
     * Creates a new Latch with the given mainLock.
     *
     * @param mainLock the mainLock
     * @throws NullPointerException          if mainLock is <tt>null</tt>.
     * @throws UnsupportedOperationException if the mainLock doesn't support the
     *                                       creation of a new Condition.
     */
    public Latch(Lock mainLock) {
        if (mainLock == null) throw new NullPointerException();
        this.mainLock = mainLock;
        this.isOpenCondition = mainLock.newCondition();
    }

    /**
     * Returns the main lock.
     *
     * @return the main lock.
     */
    public Lock getMainLock() {
        return mainLock;
    }

    /**
     * Returns the open Condition.
     *
     * @return the open condition.
     */
    public Condition getOpenCondition() {
        return isOpenCondition;
    }

    /**
     * Returns <tt>true</tt> if this Latch is open, <tt>false</tt> otherwise.
     *
     * @return <tt>true</tt> if this Latch is open, <tt>false</tt> otherwise.
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Opens this Latch. If the Latch already is open, the call is ignored. All threads that are
     * waiting on the Latch, are signalled and continue.
     * <p/>
     * This call could deadlock if the mainLock is not reentrant, and the lock already is acquired.
     *
     * @see #openWithoutLocking()
     */
    public void open() {
        if (open)
            return;

        mainLock.lock();
        try {
            open = true;
            isOpenCondition.signalAll();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Opens this Latch. If the Latch already is open, the call is ignored. All threads that are
     * waiting on the Latch are signalled and continue. The difference between {@link #open()} is that the
     * lock already must have been acquired. If this lock is not acquired,
     *
     * @see #open()
     */
    public void openWithoutLocking() {
        if (open)
            return;

        isOpenCondition.signalAll();
        open = true;
    }

    /**
     * Awaits for this Latch to open. If the Latch already is open, the call finishes immediately
     * and no InterruptedException is thrown.
     *
     * @throws InterruptedException if the calling thread is interrupted.
     */
    public void await() throws InterruptedException {
        if (open)
            return;

        mainLock.lockInterruptibly();
        try {
            while (!open)
                isOpenCondition.await();
        } finally {
            mainLock.unlock();
        }
    }

    public boolean tryAwait() {
        return open;
    }

    public void tryAwait(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        long timeoutNs = toUsableNanos(timeout, unit);

        if (open)
            return;

        mainLock.lockInterruptibly();
        try {
            while (!open)
                timeoutNs = awaitNanosAndThrow(isOpenCondition, timeoutNs);
        } finally {
            mainLock.unlock();
        }
    }
}
