/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.lendablereference;

import org.codehaus.prometheus.references.LendableReference;
import org.codehaus.prometheus.waitpoint.Waitsection;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LendableReferenceWithEnteringWaitpoint<E> implements LendableReference<E> {
    private final LendableReference<E> target;
    private final Waitsection waitpoint;

    public LendableReferenceWithEnteringWaitpoint(LendableReference<E> target, Waitsection waitpoint) {
        this.target = target;
        this.waitpoint = waitpoint;
    }

    public E take() throws InterruptedException {
        waitpoint.enter();
        return target.take();
    }


    public boolean isTakePossible() {
        //todo
        throw new RuntimeException();
    }

    public E tryTake(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        throw new RuntimeException();
    }


    public void takebackAndReset(E ref) {
        //todo
        throw new RuntimeException();
    }

    public void takeback(E ref) {
        waitpoint.exit();
        target.takeback(ref);
    }

    public E put(E newRef) throws InterruptedException {
        throw new RuntimeException();
    }

    public E tryPut(E newRef, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        throw new RuntimeException();
    }

    public E peek() {
        throw new RuntimeException();
    }

    public E tryTake() {
        throw new RuntimeException();
    }
}
