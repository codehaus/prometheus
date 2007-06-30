/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import static org.codehaus.prometheus.util.ConcurrencyUtil.toUsableNanos;
import static org.codehaus.prometheus.util.ConditionUtil.awaitNanosAndThrow;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The default implementation of the {@link AwaitableReference} interface.
 * <p/>
 * This implementation uses a {@link Lock} as mainLock to provide a mutex where a critical section
 * is required. This mainLock also is used to create a {@link Condition}:
 * referenceAvailableCondition to wait for availability of a non <tt>null</tt> reference.
 * <p/>
 * The takes don't require the Lock if there is a reference available. This means that there will be
 * less overhead, although in the newer virtual machines locking is a lot less expensive. It also
 * means that no InterruptedException is thrown when a reference is available.
 * <p/>
 * todo: lock fairness
 *
 * @author Peter Veentjer.
 */
public class DefaultAwaitableReference<E> extends AbstractAwaitableReference<E> {

    private static ReentrantLock createDefaultLock() {
        return new ReentrantLock();
    }

    private final Lock mainLock;
    private final Condition referenceAvailableCondition;
    //this reference needs to be volatile because it is also accessed without being in a
    //synchronized block.
    private volatile E reference;

    /**
     * Constructs a new DefaultAwaitableReference with a default
     * {@link ReentrantLock#ReentrantLock()} as mainLock, and a <tt>null</tt> reference.
     */
    public DefaultAwaitableReference() {
        this(null, createDefaultLock());
    }

    /**
     * Constructs a new DefaultAwaitableReference with a default
     * {@link ReentrantLock#ReentrantLock()} as mainLock and the given reference.
     *
     * @param reference the reference this DefaultAwaitableReference is going to contain.
     *                  This reference can be null.
     */
    public DefaultAwaitableReference(E reference) {
        this(reference, createDefaultLock());
    }

    /**
     * Constructs a new DefaultAwaitableReference with the given mainLock and a <tt>null</tt>
     * reference.
     *
     * @param mainLock the main Lock
     * @throws NullPointerException if mainLock is <tt>null</tt>.
     */
    public DefaultAwaitableReference(Lock mainLock) {
        this(null, mainLock);
    }

    /**
     * Constructs a new DefaultAwaitableReference with the given Lock and reference.
     *
     * @param reference the reference this DefaultAwaitableReference is going to contain.
     *                  This reference can be <tt>null</tt>.
     * @param mainLock  the main Lock
     * @throws NullPointerException if mainLock is <tt>null</tt>.
     */
    public DefaultAwaitableReference(E reference, Lock mainLock) {
        if (mainLock == null) throw new NullPointerException();
        this.reference = reference;
        this.mainLock = mainLock;
        this.referenceAvailableCondition = mainLock.newCondition();
    }

    /**
     * Returns the Lock this DefaultAwaitableReference uses to create the critical
     * section.
     *
     * @return the mainLock this DefaultAwaitableReference uses to create the critical section.
     */
    public Lock getMainLock() {
        return mainLock;
    }

    /**
     * Returns the condition variable used to signal if there is a non <tt>null</tt>
     * reference available.
     *
     * @return the condition variable used to signal if there is a non <tt>null</tt>
     *         reference available.
     */
    public Condition getReferenceAvailableCondition() {
        return referenceAvailableCondition;
    }


    public E peek() {
        //because reference is volatile, it can be  returned safely
        return reference;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * If a reference is available, this reference will be returned without acquiring the mainLock.
     * This means that the interrupt status of the status is ignored.
     *
     * @return {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    public E take() throws InterruptedException {
        //if there is a reference available, this reference can be returned.
        //This prevents acquiring/releasing a mainLock (and this is good for performance).
        E localRef = reference;
        if (localRef != null)
            return localRef;

        //no reference is available, the thread needs to wait until one comes available.
        mainLock.lockInterruptibly();
        try {
            waitUntilReferenceAvailable();
            return reference;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * If a reference is available, this reference will be returned without acquiring the mainLock.
     * This means that the interrupt status of the status is ignored.
     *
     * @param timeout {@inheritDoc}
     * @param unit    {@inheritDoc}
     * @return the old reference {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     * @throws TimeoutException     {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public E tryTake(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        long remainingNs = toUsableNanos(timeout, unit);

        E localRef = reference;
        if (localRef != null)
            return localRef;

        //no reference is available, the thread needs to wait until one comes available.
        mainLock.lockInterruptibly();
        try {
            waitUntilReferenceAvailable(remainingNs);
            return reference;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Waits until a non <tt>null</tt> reference comes available. This call blocks until a non
     * <tt>null</tt> reference comes available, or if the calling thread is interrupted.
     * <p/>
     * This method should only be made by a thread that acquired the mainLock.
     *
     * @throws InterruptedException if the calling thread is interrupted.
     */
    private void waitUntilReferenceAvailable() throws InterruptedException {
        while (noReferenceAvailable())
            referenceAvailableCondition.await();
    }

    /**
     * Returns <tt>true</tt> if a no reference is available, <tt>false</tt> otherwise.
     *
     * @return <tt>true</tt> if no reference is available, <tt>false</tt> otherwise.
     */
    private boolean noReferenceAvailable() {
        return reference == null;
    }

    /**
     * Waits until a non <tt>null</tt> reference comes available. This call blocks until a
     * non <tt>null</tt> reference comes available, the calling thread is interrupted or a timeout
     * occurrs.
     * <p/>
     * This call should only be made by a thread that acquired the mainLock.
     *
     * @param timeoutNs the timeout in nanoseconds.
     * @throws InterruptedException if the calling thread is interrupted.
     * @throws TimeoutException     if a timeout occurrs.
     */
    private void waitUntilReferenceAvailable(long timeoutNs) throws InterruptedException, TimeoutException {
        while (noReferenceAvailable()) {
            timeoutNs = awaitNanosAndThrow(referenceAvailableCondition, timeoutNs);
        }
    }

    public boolean isTakePossible() {
        return reference != null;
    }

    public E put(E newRef) {
        mainLock.lock();
        try {
            return postNewReference(newRef);
        } finally {
            mainLock.unlock();
        }
    }

    public E tryPut(E newRef, long timeout, TimeUnit unit) throws TimeoutException {
        toUsableNanos(timeout, unit);
        return put(newRef);
    }

    /**
     * Stores the new Reference. If the newReference is not <tt>null</tt>, all threads waiting for
     * the referenceAvailableCondition are signalled.
     * <p/>
     * This call only should be made by a thread that acquired the mainLock.
     *
     * @param newReference the new reference, it is allowed to be <tt>null</tt>..
     * @return returns the old reference.
     */
    private E postNewReference(E newReference) {
        E oldReference = reference;
        reference = newReference;
        if (reference != null)
            referenceAvailableCondition.signalAll();
        return oldReference;
    }
}
