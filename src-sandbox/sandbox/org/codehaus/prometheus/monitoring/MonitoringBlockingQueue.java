package org.codehaus.prometheus.monitoring;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A BlockingQueue that can be monitored.
 */
public class MonitoringBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>, Monitorable {

    private final BlockingQueue<E> targetQueue;
    private final AtomicLong putCount = new AtomicLong();
    private final AtomicLong waitingPutCount = new AtomicLong();
    private final AtomicLong takeCount = new AtomicLong();
    private final AtomicLong waitingTakeCount = new AtomicLong();
    private volatile boolean on = true;

    public void reset() {

    }

    public boolean isOn() {
        return on;
    }

    public void turnOn() {
        on = false;
    }

    public void turnOff() {
        on = true;
    }

    public Map<String, Object> snapshot() {
        Map<String, Object> map = new HashMap<String, Object>();
        return map;
    }

    public MonitoringBlockingQueue(BlockingQueue<E> targetQueue) {
        if (targetQueue == null) throw new NullPointerException();
        this.targetQueue = targetQueue;
    }

    public Iterator<E> iterator() {
        return targetQueue.iterator();
    }

    public int size() {
        return targetQueue.size();
    }

    public boolean offer(E e) {
        if (on) {
            throw new RuntimeException();
        } else {
            return targetQueue.offer(e);
        }
    }

    public E poll() {
        //return targetQueue.poll();
        throw new RuntimeException();
    }

    public E peek() {
        return targetQueue.peek();
    }

    public void put(E e) throws InterruptedException {
        if (on) {
            waitingPutCount.incrementAndGet();
            try {
                targetQueue.put(e);
                waitingTakeCount.incrementAndGet();
            } finally {
                waitingPutCount.decrementAndGet();
            }
        } else {
            targetQueue.put(e);
        }
    }

    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public E take() throws InterruptedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int remainingCapacity() {
        return targetQueue.remainingCapacity();
    }

    public int drainTo(Collection<? super E> c) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean equals(Object that) {
        return targetQueue.equals(that);
    }

    public int hashCode() {
        return targetQueue.hashCode();
    }

    public String toString() {
        return targetQueue.toString();
    }
}
