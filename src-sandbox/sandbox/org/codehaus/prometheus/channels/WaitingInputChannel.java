/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.channels;

import org.codehaus.prometheus.waitpoint.Waitpoint;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An InputChannel that uses a Waitpoint.
 *
 * @author Peter Veentjer.
 */
public class WaitingInputChannel<E> extends AbstractInputChannel<E> {
    private final InputChannel<E> target;
    private final Waitpoint waitpoint;

    public WaitingInputChannel(InputChannel<E> target, Waitpoint waitpoint) {
        this.target = target;
        this.waitpoint = waitpoint;
    }

    public InputChannel<E> getTarget() {
        return target;
    }

    public Waitpoint getWaitpoint() {
        return waitpoint;
    }

    public E poll() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public E take() throws InterruptedException {
        waitpoint.pass();
        return target.take();
    }

    public E poll(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        long remainingTimeoutNs = waitpoint.tryPass(timeout, unit);
        return target.poll(remainingTimeoutNs, TimeUnit.NANOSECONDS);
    }

    public E peek() {
        return target.peek();
    }
}
