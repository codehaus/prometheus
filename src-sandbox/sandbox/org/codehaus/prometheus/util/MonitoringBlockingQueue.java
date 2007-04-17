/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Peter Veentjer.
 */
public class MonitoringBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E> {

    private final AtomicLong nrTakes = new AtomicLong();
    private final AtomicLong nrPuts = new AtomicLong();
    private final AtomicLong nrSuccessOffers = new AtomicLong();
    private final AtomicLong nrFailedOffers = new AtomicLong();
    private final long startTimeNs = System.nanoTime();
    private final AtomicLong totalQueueSize = new AtomicLong();
    private final AtomicLong nrTicks = new AtomicLong();
    private volatile boolean activated;

    private final BlockingQueue<E> target;

    public MonitoringBlockingQueue(BlockingQueue<E> target) {
        if (target == null) throw new NullPointerException();
        this.target = target;
    }

    public double calcAddFrequency() {
        throw new RuntimeException();
    }

    public void activate() {
        activated = true;
    }

    public void resetAndActivate() {
        reset();
    }

    public void deactivate() {
        activated = false;
    }

    public boolean isActivated() {
        return activated;
    }

    public long calcTotalAdds() {
        return nrPuts.longValue() + nrSuccessOffers.longValue();
    }

    public double calcAverageSize() {
        return totalQueueSize.longValue() / nrTicks.longValue();
    }

    public double calcAverageWaittimeForAdd() {
        throw new RuntimeException();
    }

    public double calcAverageWaittimeForRemove() {
        throw new RuntimeException();
    }

    /**
     * Calculates the remove frequency in HZ.
     *
     * @return
     */
    public double calcRemoveFrequency() {
        long runtimeNs = getRunningTimeNs();
        long removes = calcTotalRemoves();
        return removes / runtimeNs;
    }

    public long getRunningTimeNs() {
        return System.nanoTime() - startTimeNs;
    }

    public long calcTotalRemoves() {
        return nrTakes.longValue();//todo
    }

    public void reset() {
        nrTakes.set(0);
        nrPuts.set(0);
        nrSuccessOffers.set(0);
        nrFailedOffers.set(0);
    }
    //average queue size.

    public Iterator<E> iterator() {
        //todo: hier moet ook controle op.
        return target.iterator();
    }

    public int size() {
        return target.size();
    }

    public boolean offer(E o) {
        if (!activated) {
            return target.offer(o);
        } else {
            if (target.offer(o)) {
                nrSuccessOffers.incrementAndGet();
                return true;
            } else {
                nrFailedOffers.incrementAndGet();
                return false;
            }
        }
    }

    public E poll() {
        throw new RuntimeException();
    }

    public E peek() {
        return target.peek();
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        throw new RuntimeException();
    }

    public boolean offer(E o, long timeout, TimeUnit unit) throws InterruptedException {
        if (target.offer(o, timeout, unit)) {
            nrSuccessOffers.incrementAndGet();
            return true;
        } else {
            nrFailedOffers.incrementAndGet();
            return false;
        }
    }


    public E take() throws InterruptedException {
        if (!activated) {
            return target.take();
        } else {
            E item = target.take();
            nrTakes.incrementAndGet();
            return item;
        }
    }

    public void put(E o) throws InterruptedException {
        if (activated) {
            target.put(o);
        } else {
            target.put(o);
            nrPuts.incrementAndGet();
        }
    }

    public int remainingCapacity() {
        return target.remainingCapacity();
    }

    public int drainTo(Collection<? super E> c) {
        throw new RuntimeException();
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        throw new RuntimeException();
    }
}
