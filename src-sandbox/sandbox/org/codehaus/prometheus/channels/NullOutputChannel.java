package org.codehaus.prometheus.channels;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;

public class NullOutputChannel<E> implements OutputChannel<E>{

    public final static NullInputChannel instance = new NullInputChannel();

    public void put(E item) throws InterruptedException {
        if(item == null)throw new NullPointerException();
    }

    public void savePut(E item) throws InterruptedException, TimeoutException {
        if(item == null)throw new NullPointerException();
    }

    public long offer(E item, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if(item == null||unit == null)throw new NullPointerException();
        return unit.toNanos(timeout);
    }
}
