package org.codehaus.prometheus.references;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class NonBlockingAwaitableReference<E> implements AwaitableReference<E> {

    private final AtomicReference<E> reference = new AtomicReference<E>();

    public boolean isTakePossible() {
        return reference.get()!=null;
    }

    public E take() throws InterruptedException {
        //todo
        return null;
    }

    public E tryTake() {
        return reference.get();
    }

    public E tryTake(long timeout, TimeUnit unit) throws TimeoutException {
        //todo
        return null;
    }

    public E put(E newRef) {
        return reference.getAndSet(newRef);
    }

    public E tryPut(E newRef, long timeout, TimeUnit unit) throws TimeoutException {
        return reference.getAndSet(newRef);
    }

    public E peek() {
        return reference.get();
    }
}
