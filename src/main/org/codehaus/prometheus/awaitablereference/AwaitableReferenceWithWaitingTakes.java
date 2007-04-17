/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.awaitablereference;

import org.codehaus.prometheus.waitpoint.Waitpoint;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An AwaitableReference decorater that adds control on the taking of references by using a
 * {@link Waitpoint}.
 * <p/>
 * It could used to open or close a {@link AwaitableReference} so no threads are able to take a
 * reference.
 * <p/>
 * Example of an AwaitableReference where the takes are controlled by a
 * {@link org.codehaus.prometheus.waitpoint.CloseableWaitpoint}.
 * <pre>
 * AwaitableReference&lt;Integer&gt; target = new DefaultAwaitableReference&lt;Integer&lt;();
 * CloseableWaitpoint closeableWaitpoint = new CloseableWaitpoint();
 * AwaitableReference&lt;Integer&gt; awaitableRef =
 *      new AwaitableReferenceWithWaitingTakes&lt;Integer,CloseableWaitpoint&gt;(target,closeableWaitpoint); 
 * </pre>
 * It also could be used to throttle the taking of references. At the moment there is no waitpoint
 * with throttling behavior, but it is planned.
 *
 * @author Peter Veentjer.
 */
public class AwaitableReferenceWithWaitingTakes<E, W extends Waitpoint> extends AbstractAwaitableReference<E> {
    private final AwaitableReference<E> target;
    private final W waitPoint;

    /**
     * Constructs a new AwaitableReferenceWithWaitingTakes
     *
     * @param target the AwaitableReference to decorate.
     * @param waitPoint the Waitpoint used 
     * @throws NullPointerException if target or waitPoint is <tt>null</tt>.
     */
    public AwaitableReferenceWithWaitingTakes(AwaitableReference<E> target, W waitPoint) {
        if (target == null || waitPoint == null) throw new NullPointerException();
        this.target = target;
        this.waitPoint = waitPoint;
    }

    /**
     * Returns the AwaitableReference that is being decorated by this
     * AwaitableReferenceWithWaitingTakes.
     *
     * @return the target AwaitableReference.
     */
    public AwaitableReference<E> getTarget() {
        return target;
    }

    /**
     * Returns the Waitpoint.
     *
     * @return the waitpoint.
     */
    public W getWaitpoint() {
        return waitPoint;
    }

    public E take() throws InterruptedException {
        waitPoint.pass();
        return target.take();
    }

    public E tryTake(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        long remainingNs = waitPoint.tryPass(timeout, unit);
        return target.tryTake(remainingNs, TimeUnit.NANOSECONDS);
    }

    public E peek() {
        return target.peek();
    }

    public boolean isTakePossible() {
        return waitPoint.isPassible();
    }

    public E put(E newRef) throws InterruptedException {
        return target.put(newRef);
    }

    public E tryPut(E newRef, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        return target.tryPut(newRef,timeout,unit);
    } 
}
