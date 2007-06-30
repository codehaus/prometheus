package org.codehaus.prometheus.channels;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 */
public class NullInputChannel<E> implements InputChannel<E> {

    public final static NullInputChannel INSTANCE = new NullInputChannel();

    public E take() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    public E poll() {
        return null;
    }

    public E poll(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        if (unit == null) throw new NullPointerException();
        return null;
    }

    public E peek() {
        return null;
    }
}
