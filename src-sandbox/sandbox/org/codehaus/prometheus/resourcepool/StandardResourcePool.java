/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.resourcepool;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Peter Veentjer.
 */
public class StandardResourcePool<E> implements ResourcePool<E>{

    public StandardResourcePool(){}

    public void takeback(E e) {
        throw new RuntimeException();
    }

    public E take() throws InterruptedException {
        throw new RuntimeException();
    }

    public E takeUninterruptibly() {
        throw new RuntimeException();
    }

    public E peek() {
        throw new RuntimeException();
    }

    public boolean isTakePossible() {
        throw new RuntimeException();
    }

    public E tryTake() {
        throw new RuntimeException();
    }

    public E tryTake(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        throw new RuntimeException();
    }

    public E tryTakeUninterruptibly(long timeout, TimeUnit unit) throws TimeoutException {
        throw new RuntimeException();
    }
}
