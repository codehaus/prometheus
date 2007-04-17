package org.codehaus.prometheus.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;

/**
 * A {@link java.util.concurrent.BlockingQueue} that eats up all messages that are put on it. Other methods are not
 * supported. A SinkBlockingQueue is convenient when a BlockingQueue is needed, but you are interrested in the items
 * that are put. If a normal {@link java.util.concurrent.BlockingQueue} implementation was used, it could lead to
 * failing offer or blocking put if it is bounded, or to memory problems when it is unbounded. 
 *
 */
public final class SinkBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E> {

    public Iterator<E> iterator() {
        return Collections.EMPTY_LIST.iterator();
    }

    public int size() {
        return 0;
    }

    public boolean offer(E o) {
        if(o == null)throw new NullPointerException();
        return true;
    }

    public E poll() {
        throw new UnsupportedOperationException();
    }

    public E peek() {
        throw new UnsupportedOperationException();
    }

    public void put(E o) throws InterruptedException {
        if(o == null)throw new NullPointerException();
    }

    public boolean offer(E o, long timeout, TimeUnit unit) throws InterruptedException {
        if(o == null || unit == null)throw new NullPointerException();
        return true;
    }

    public E take() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    public int remainingCapacity() {
        throw new UnsupportedOperationException();
    }

    public int drainTo(Collection c) {
        throw new UnsupportedOperationException();
    }

    public int drainTo(Collection c, int maxElements) {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object thatObj){
        //todo
        throw new RuntimeException();
    }

    public int hashCode(){
        //todo
        throw new RuntimeException();
    }

    public String toString(){
        //todo
        throw new RuntimeException();
    }
}
