/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link LendableReference} that allows a new reference to be set, before all lend references
 * have returned. This means that different references could be lend at any given moment. If this
 * isn't acceptable, you could have a look at the {@link StrictLendableReference}.
 * <p/>
 * The RelaxedLendableReference extends the DefaultAwaitableReference because this implementation
 * provides most functionality that is required.  
 * <p/>
 * Because setting of a new reference doesn't block when old references are lend, and because lending a
 * value doesn't block as long as a non null reference is available, there is not as much lock
 * contention as with the StrictLendableReference. This means that the LendableReference provides
 * better concurrent behaviour.
 *
 * @author Peter Veentjer.
 * @see StrictLendableReference
 * @see org.codehaus.prometheus.references.DefaultAwaitableReference
 */
public class RelaxedLendableReference<E> extends DefaultAwaitableReference<E> implements LendableReference<E> {

    /**
     * Creates a new RelaxedLendableReference with <tt>null</tt> as current reference, and a default
     * {@link ReentrantLock#ReentrantLock()}.
     */
    public RelaxedLendableReference() {
    }

    /**
     * Creates a new RelaxedLendableReference with a default {@link ReentrantLock#ReentrantLock()}
     * as mainLock and the given reference.
     *
     * @param ref the reference that is placed in this RelaxedLendableReference. This is allowed to
     *            be <tt>null</tt>.
     */
    public RelaxedLendableReference(E ref) {
        super(ref);
    }

    /**
     * Creates a new RelaxedLendableReference with the given mainLock and a <tt>null</tt> as
     * reference.
     *
     * @param mainLock the mainLock.
     * @throws NullPointerException if mainLock is <tt>null</tt>.
     */
    public RelaxedLendableReference(Lock mainLock) {
        this(null, mainLock);
    }

    /**
     * Creates a new RelaxedLendableReference with the given Lock and reference.
     *
     * @param ref      the reference that is placed in this RelaxedLendableReference. This is
     *                 allowed to be <tt>null</tt>.
     * @param mainLock the mainLock
     * @throws NullPointerException if mainLock is <tt>null</tt>.
     */
    public RelaxedLendableReference(E ref, Lock mainLock) {
        super(ref, mainLock);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This method doesn't do anything apart from checking if the reference is not null. With the
     * RelaxedLendableReference it is possible that at any given moment multiple references are
     * lend, and taking back doesn't do anything unlike the take back of the
     * StrictLendableReference. So incorrect references that are taking back, or not detected.
     */
    public void takeback(E ref) {
        if (ref == null) throw new NullPointerException();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * The reference is only reset when the current reference is equal to the reference that is
     * taken back.
     */
    public void takebackAndReset(E ref) {
        if (ref == null) throw new NullPointerException();
        conditionalReset(ref);
    }
}
