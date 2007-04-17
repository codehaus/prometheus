/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import org.codehaus.prometheus.closeable.Closeable;
import org.codehaus.prometheus.waitpoint.CloseableWaitpoint;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ipv de closable, een tryClose voor de input kant, en een tryClose voor de outpuntkant.
 * misschien een idee om de queue 2 objecten te laten beheren die verantwoordelijk
 * zijn van het implementeren van de in-closable en out-closable.
 *
 * @author Peter Veentjer.
 */
public class CloseableBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E> {
    private final BlockingQueue<E> blockingQueue;
    private final Lock mainLock;
    private final CloseableWaitpoint frontCloseable = new CloseableWaitpoint();
    private final CloseableWaitpoint backCloseable = new CloseableWaitpoint();

    public CloseableBlockingQueue(BlockingQueue<E> blockingQueue, boolean open) {
        this.mainLock = new ReentrantLock();
        this.blockingQueue = blockingQueue;
        //this.open = open;
    }

    public Closeable getFrontClosable(){
        return frontCloseable;
    }

    public Closeable getBackCloneable(){
        return backCloseable;
    }

    public Iterator<E> iterator() {
        throw new RuntimeException();
    }

    public int size() {
        return blockingQueue.size();
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
}
