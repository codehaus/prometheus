/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.waitpoint;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The BlockingQueueWithWaitingTakesAndPuts is a BlockingQueue that
 * decorates a target BlockingQueue. It also contains a pair of
 * associated Waitpoints:
 * <ol>
 * <li>
 * <b>frontWaitpoint</b>:
 * </li>
 * <li>
 * <b>backWaitpoint</b>:
 * </li>
 * </ol>
 * <p/>
 * What is it useful for?
 * -creating a BlockingQueue that can be opened and closed.
 * frontend-close/backend-close
 * -creating a BlockingQueue where the takes an puts can be
 * throttled.
 *
 * @author Peter Veentjer.
 */
public class BlockingQueueWithWaitingTakesAndPuts<E, FW extends Waitpoint, BW extends Waitpoint>
        extends AbstractQueue<E>
        implements BlockingQueue<E> {

    private final FW frontWaitpoint;
    private final BW backWaitpoint;
    private final BlockingQueue<E> targetQueue;

    public BlockingQueueWithWaitingTakesAndPuts(BlockingQueue<E> queue, FW frontWaitpoint, BW backWaitpoint) {
        this.targetQueue = queue;
        this.frontWaitpoint = frontWaitpoint;
        this.backWaitpoint = backWaitpoint;
    }

    public FW getFrontWaitpoint() {
        return frontWaitpoint;
    }

    public BW getBackWaitpoint() {
        return backWaitpoint;
    }

    public BlockingQueue<E> getTargetQueue() {
        return targetQueue;
    }

    public Iterator<E> iterator() {
        return targetQueue.iterator();
    }

    public int size() {
        return targetQueue.size();
    }

    public boolean offer(E o) {
        throw new RuntimeException();
        /*
        try {
            frontWaitpoint.tryPassUninterruptibly(0,TimeUnit.NANOSECONDS);
            return targetQueue.offer(o);
        } catch (TimeoutException e) {
            return false;
        } */
    }

    public E poll() {
        /*
        frontWaitpoint.passUninterruptibly();
        return targetQueue.poll();*/
        throw new RuntimeException();
    }

    public E peek() {
        return targetQueue.peek();
    }

    public boolean offer(E o, long timeout, TimeUnit unit) throws InterruptedException {
        /*
        try {
            long remainingNs = frontWaitpoint.tryPassUninterruptibly(timeout,unit);
            return targetQueue.offer(o,remainingNs,TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
            return false;
        }*/
        throw new RuntimeException();
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        throw new RuntimeException();
    }

    public E take() throws InterruptedException {
        backWaitpoint.pass();
        return targetQueue.take();
    }

    public void put(E o) throws InterruptedException {
        frontWaitpoint.pass();
        targetQueue.put(o);
    }

    public int remainingCapacity() {
        return targetQueue.remainingCapacity();
    }

    public int drainTo(Collection<? super E> c) {
        throw new RuntimeException();//todo
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        throw new RuntimeException();//todo
    }
}
