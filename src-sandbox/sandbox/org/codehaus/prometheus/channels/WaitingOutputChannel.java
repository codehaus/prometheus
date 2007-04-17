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
 * A OutputChannel that decorates a target OutputChannel with a waitpoint.
 *
 * @author Peter Veentjer.
 */
public class WaitingOutputChannel<E> extends AbstractOutputChannel<E> {
    private final OutputChannel<E> target;
    private final Waitpoint waitpoint;

    public WaitingOutputChannel(OutputChannel<E> target, Waitpoint waitpoint){
        if(target == null||waitpoint == null)throw new NullPointerException();
        this.target = target;
        this.waitpoint = waitpoint;
    }

    public OutputChannel<E> getTarget() {
        return target;
    }

    public Waitpoint getWaitpoint() {
        return waitpoint;
    }

    public void put(E item) throws InterruptedException {
        waitpoint.pass();
        target.put(item);
    }

    public long offer(E item, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        long remainingTimeoutNs = waitpoint.tryPass(timeout,unit);
        return target.offer(item,remainingTimeoutNs,TimeUnit.NANOSECONDS);
    }
}
