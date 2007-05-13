/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.lendablereference;

import org.codehaus.prometheus.waitpoint.Waitpoint;
import org.codehaus.prometheus.references.LendableReference;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A LendableReference decorator. It decorates a target {@link org.codehaus.prometheus.references.LendableReference} and lets all takes
 * wait, this is done by using a {@link Waitpoint}. This structure can be used to control the taking
 * of references; for example:
 * <ol>
 *      <li>to close a LendableReference, see
 *          {@link org.codehaus.prometheus.waitpoint.CloseableWaitpoint}
 *      </li>
 *      <li>to throttle a LendableReference, see
 *          {@link org.codehaus.prometheus.waitpoint.ThrottlingWaitpoint}
 *      </li>
 * </ol>
 *
 * <p/>
 * idea:
 * a LendableReference with a limit on the number of lend references. this can only be done with an 
 * enter/exit-waitpoint.
 *
 * @see org.codehaus.prometheus.references.AwaitableReferenceWithWaitingTakes
 * @author Peter Veentjer.
 */
public class LendableReferenceWithWaitingTakes<E,W extends Waitpoint> implements LendableReference<E> {
    private final LendableReference<E> target;
    private final W waitPoint;

    /**
     * Constructs a new LendableReferenceWithWaitingTakes
     *
     * @param target the LendableReference that is being 'controlled'.
     * @param waitPoint the Waitpoint used to do the controlling.
     * @throws NullPointerException if target or waitPoint is null.
     */
    public LendableReferenceWithWaitingTakes(LendableReference<E> target, W waitPoint) {
        if (target == null || waitPoint == null) throw new NullPointerException();
        this.target = target;
        this.waitPoint = waitPoint;
    }

    /**
     * Returns the target LendableReference.
     *
     * @return the target LendableReference.
     */
    public LendableReference<E> getTarget() {
        return target;
    }

    /**
     * Returns the Waitpoint.
     * 
     * @return the Waitpoint used.
     */
    public W getWaitpoint() {
        return waitPoint;
    }


    public boolean isTakePossible() {
        return waitPoint.isPassible();
    }

    /**
     * {@inheritDoc}
     *
     * Before doing the tryTake, {@link Waitpoint#pass()} is called.
     *
     * @return {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}.
     */
    public E take() throws InterruptedException {
        waitPoint.pass();
        return target.take();
    }

    public void takebackAndReset(E ref) {
        target.takebackAndReset(ref);        
    }

    public E tryTake(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        long remainingTimeoutNs = waitPoint.tryPass(timeout, unit);
        return target.tryTake(remainingTimeoutNs, TimeUnit.NANOSECONDS);
    }

    public void takeback(E ref) {
        target.takeback(ref);
    }

    /**
     * {@inheritDoc}
     *
     * The Waitpoint is not called for this method.
     *
     * @param newRef {@inheritDoc}
     * @return {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    public E put(E newRef) throws InterruptedException {
        return target.put(newRef);
    }

    /**
     * {@inheritDoc}
     *
     * The Waitpoint is not called for this method.
     *
     * @param newRef {@inheritDoc}
     * @param timeout {@inheritDoc}
     * @param unit {@inheritDoc}
     * @return {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     * @throws TimeoutException {@inheritDoc}
     */
    public E tryPut(E newRef, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        return target.tryPut(newRef, timeout, unit);
    }

    /**
     * {@inheritDoc}
     *
     * The Waitpoint is not called for this method.
     *
     * @return {@inheritDoc}
     */
    public E peek() {
        return target.peek();
    }

    public E tryTake() {
        throw new RuntimeException();
    }
}
