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

/**
 * @author Peter Veentjer
 */
public class ThrothelingBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E> {
    private final BlockingQueue<E> target;

    public ThrothelingBlockingQueue(BlockingQueue<E> target) {
        this.target = target;
    }

    public BlockingQueue<E> getTarget() {
        return target;
    }

    public Iterator<E> iterator() {
        return target.iterator();
    }

    public int size() {
        return target.size();
    }

    public boolean offer(E o) {
        throw new RuntimeException();
    }

    public E poll() {
        throw new RuntimeException();
    }

    public E peek() {
        throw new RuntimeException();
    }

    public boolean offer(E o, long timeout, TimeUnit unit) throws InterruptedException {
        throw new RuntimeException();
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        throw new RuntimeException();
    }

    public E take() throws InterruptedException {
        throw new RuntimeException();
    }

    public void put(E o) throws InterruptedException {
        throw new RuntimeException();
    }

    public int remainingCapacity() {
        throw new RuntimeException();
    }

    public int drainTo(Collection<? super E> c) {
        throw new RuntimeException();
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        throw new RuntimeException();
    }

    public boolean equals(Object thatObj) {
        throw new RuntimeException();
    }

    public int hashCode() {
        throw new RuntimeException();
    }
}
