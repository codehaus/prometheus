/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.waitpoint;

import org.codehaus.prometheus.closeable.Closeable;
import static org.codehaus.prometheus.util.ConcurrencyUtil.toUsableNanos;
import static org.codehaus.prometheus.util.ConditionUtil.awaitNanosOrThrow;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link org.codehaus.prometheus.waitpoint.Waitpoint} that can be opened or closed. If the Waitpoint
 * is open,all pass requests continue without blocking. If the Waitpoint is closed, all pass
 * requests block until it is opened.
 * <p/>
 * A CloseableWaitpoint can be used to close/open a structure, like the BlockingQueue for example.
 * <p/>
 * <td><b>CloseableWaitpoint vs Latch</b></td>
 * <dd>
 * Unlike a latch (like the {@link java.util.concurrent.Future} or the
 * {@link java.util.concurrent.CountDownLatch}, a CloseableWaitpoint can be closed after it has been
 * open, and opened after it has been closed. So there is no final state.
 * </dd>
 * <p/>
 * todo:
 * optional locken. If a lock is passed from the outside, and on the outside already locking takes
 * place, a lock doesn't need to be obtained. With reentrant locks it wouldn't matter.
 *
 * @author Peter Veentjer.
 */
public class CloseableWaitpoint extends AbstractWaitpoint implements Closeable {

    /**
     * Creates a new unfair ReentrantLock.
     *
     * @return a new unfair ReentrantLock.
     */
    public static Lock newDefaultMainLock() {
        return new ReentrantLock();
    }

    private final Lock mainLock;
    private final Condition isOpenCondition;
    private volatile boolean open;

    /**
     * Creates a new open CloseableWaitpoint with a default mainLock.
     * ({@link ReentrantLock#ReentrantLock()}
     */
    public CloseableWaitpoint() {
        this(newDefaultMainLock(), true);
    }

    /**
     * Creates a new CloseableWaitpoint with a default mainLock
     * ({@link ReentrantLock#ReentrantLock()}
     *
     * @param open if CloseableWaitpoint is open
     */
    public CloseableWaitpoint(boolean open) {
        this(newDefaultMainLock(), open);
    }


    /**
     * Constructs a new CloseableWaitpoint with the given mainLock
     * and a flag indicating if this CloseableWaitpoint should be open.
     *
     * @param mainLock the mainLock
     * @param open     <tt>true</tt> if the CloseableWaitpoint is open, <tt>false</tt> if it
     *                 it should be closed
     * @throws UnsupportedOperationException if the mainLock isn't able to create a Condition,
     * @throws NullPointerException          if mainLock is <tt>null</tt>
     * @see Lock#newCondition()
     */
    public CloseableWaitpoint(Lock mainLock, boolean open) {
        if (mainLock == null) throw new NullPointerException();
        this.mainLock = mainLock;
        this.isOpenCondition = mainLock.newCondition();
        this.open = open;
    }


    /**
     * Returns the main lock.
     *
     * @return the mainLock.
     */
    public Lock getMainLock() {
        return mainLock;
    }

    /**
     * Returns the Condition that is used to wait for the open conditionvariable.
     *
     * @return the Condition
     */
    public Condition getOpenCondition() {
        return isOpenCondition;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isClosed() {
        return !open;
    }

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

    public void close() {
        open = false;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * No InterruptedException is thrown when this CloseableWaitpoint is open and the interrupt flag
     * of the calling thread is set.
     *
     * @throws InterruptedException {@inheritDoc}
     */
    public void pass() throws InterruptedException {
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

    /**
     * {@inheritDoc}
     * <p/>
     * No InterruptedException is thrown when this CloseableWaitpoint is open and the interrupt flag
     * of the calling thread is set.
     *
     * @param timeout {@inheritDoc}
     * @param unit    {@inheritDoc}
     * @return {@inheritDoc}
     * @throws TimeoutException     {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    public long tryPass(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        long timeoutNs = toUsableNanos(timeout, unit);

        if (open)
            return timeoutNs;

        mainLock.lockInterruptibly();
        try {
            while (!open)
                timeoutNs = awaitNanosOrThrow(isOpenCondition, timeoutNs);

            return timeoutNs;
        } finally {
            mainLock.unlock();
        }
    }

    public boolean isPassible() {
        return open;
    }
}
