/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import static org.codehaus.prometheus.util.ConcurrencyUtil.toUsableNanos;
import static org.codehaus.prometheus.util.ConditionUtil.awaitNanosOrThrow;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link LendableReference} that doesn't allow different references to be lend at the same moment. A new
 * reference can only be set, when all lend references are returned.
 * <p/>
 * The StrictLendableReference causes contention when a new reference is set, because taking/putting
 * threads are going to block until all references are returned. If it doesn't matter that different
 * values are lend at any given moment, the {@link RelaxedLendableReference} is a better performing
 * alternative.
 * <p/>
 * <td><b>Fairness</b></td>
 * <dd></dd>
 * <p/>
 * <td><b>Why the ReadWriteLock can't be used</b></td>
 * <dd>
 * ReadWriteLock can't be used because the LendableReference allows a reference to be taken back by a different
 * thread, than took the reference. Lock implementation normally only allow the same thread to take the value back.
 * </dd>
 * <p/>
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public class StrictLendableReference<E> extends AbstractAwaitableReference<E> implements LendableReference<E> {

    /**
     * Constructs a fair ReentrantLock.
     *
     * @return a fair ReentrantLock.
     */
    public static Lock newDefaultMainLock() {
        return new ReentrantLock(true);
    }

    private final Lock mainLock;
    private final Condition refAvailableCondition;
    private final Condition noTakersCondition;
    private volatile long takeCount = 0;
    private volatile E ref;
    private volatile E takebackRef;

    /**
     * Constructs a new StrictLendableReference, with a fair {@link ReentrantLock} and a
     * <tt>null</tt> reference.
     */
    public StrictLendableReference() {
        this(newDefaultMainLock(), null);
    }

    /**
     * Constructs a new StrictLendableReference with a fair {@link ReentrantLock} and the given
     * reference.
     *
     * @param ref the reference that is stored in this StrictLendableReference. This value is
     *            allowed to be <tt>null</tt>.
     */
    public StrictLendableReference(E ref) {
        this(newDefaultMainLock(), ref);
    }

    /**
     * Constructs a new StrictLendableReference that can be fair or unfair (see documentation of the
     * class for more information) and the given reference.
     *
     * @param fair if this StrictLendableReference is fair.
     * @param ref  the reference that is stored in this StrictLendableReference. This value is
     *             allowed to be <tt>null</tt>.
     */
    public StrictLendableReference(boolean fair, E ref) {
        this(new ReentrantLock(fair), ref);
    }

    /**
     * Constructs a new StrictLendableReference.
     *
     * @param mainLock the mainLock
     * @param ref      the initial reference, can be <tt>null<tt>.
     * @throws NullPointerException if mainLock is null.
     */
    public StrictLendableReference(Lock mainLock, E ref) {
        if (mainLock == null) throw new NullPointerException();
        this.ref = ref;
        this.takebackRef = ref;
        this.mainLock = mainLock;
        this.refAvailableCondition = mainLock.newCondition();
        this.noTakersCondition = mainLock.newCondition();
    }

    /**
     * Returns the main Lock.
     *
     * @return the main Lock.
     */
    public Lock getMainLock() {
        return mainLock;
    }

    /**
     * Returns the  Condition that is used to signal if a reference is available.
     *
     * @return the refAvailable Condition.
     */
    public Condition getRefAvailableCondition() {
        return refAvailableCondition;
    }

    /**
     * Returns the Condition that is used to signal if all references are returned.
     *
     * @return the noTakers condition.
     */
    public Condition getNoTakersCondition() {
        return noTakersCondition;
    }

    /**
     * Returns the number of takes. If no references are taken, 0 is returned. The value could be
     * stale at the moment it is received.
     *
     * @return the current number of takes.
     */
    public long getTakeCount() {
        return takeCount;
    }

    public boolean isTakePossible() {
        return ref != null;
    }

    public E take() throws InterruptedException {
        mainLock.lockInterruptibly();
        try {
            while (ref == null)
                refAvailableCondition.await();

            takeCount++;
            return ref;
        } finally {
            mainLock.unlock();
        }
    }

    public E tryTake(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        long timeoutNs = toUsableNanos(timeout, unit);

        mainLock.lockInterruptibly();
        try {
            while (ref == null)
                timeoutNs = awaitNanosOrThrow(refAvailableCondition, timeoutNs);

            takeCount++;
            return ref;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Decreases the takeCount. If the takeCount reaches 0, the noTakersCondition is signalled.
     * When the takeCount already is 0, an IllegalTakebackException is thrown.
     * <p/>
     * This call only should be made when the mainLock is held.
     */
    private void decreaseTakeCount() {
        ensureAtLeastOneReferenceIsTaken();

        takeCount--;
        if (takeCount == 0)
            noTakersCondition.signalAll();
    }

    /**              \
     * Checks if the ref is the correct reference for takeback.
     *
     * @param ref the argument to check. Shouldn't be null.
     */
    private void checkReferenceForTakeback(E ref) {
        if (!ref.equals(takebackRef))
            throw new IllegalTakebackException("incorrect reference is taken back");
    }

    public void takeback(E ref) {
        if (ref == null) throw new NullPointerException();

        mainLock.lock();
        try {
            checkReferenceForTakeback(ref);
            decreaseTakeCount();
        } finally {
            mainLock.unlock();
        }
    }

    public void takebackAndReset(E ref) {
        if (ref == null) throw new NullPointerException();

        mainLock.lock();
        try {
            checkReferenceForTakeback(ref);
            decreaseTakeCount();
            this.ref = null;
        } finally {
            mainLock.unlock();
        }
    }

    private void ensureAtLeastOneReferenceIsTaken() {
        if (takeCount == 0)
            throw new IllegalTakebackException("no references are lend");
    }

    /**
     * Checks if there are takes.
     *
     * @return <tt>true</tt> if there are takes, false otherwise.
     */
    private boolean hasTakers() {
        return takeCount > 0;
    }

    public E put(E newRef) throws InterruptedException {
        mainLock.lockInterruptibly();
        try {
            while (hasTakers())
                noTakersCondition.await();

            return updateReference(newRef);
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Updates the reference this StrictLendableReference holds.
     *
     * Call only should be made when the mainLock is hold.
     *
     * @param newRef   the reference to set
     * @return  the old reference.
     */
    private E updateReference(E newRef) {
        E oldRef = ref;
        this.ref = newRef;
        if (ref != null)
            refAvailableCondition.signalAll();
        takebackRef = ref;
        return oldRef;
    }

    public E tryPut(E newRef, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        long timeoutNs = toUsableNanos(timeout, unit);

        mainLock.lockInterruptibly();
        try {
            while (hasTakers())
                timeoutNs = awaitNanosOrThrow(noTakersCondition, timeoutNs);

            return updateReference(newRef);
        } finally {
            mainLock.unlock();
        }
    }

    public E peek() {
        return ref;
    }
}
