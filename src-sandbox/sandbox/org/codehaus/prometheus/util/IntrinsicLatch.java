package org.codehaus.prometheus.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A {@link Latch} implementation that makes use of an intrinsinc lock instead
 * of the {@link java.util.concurrent.locks.Lock}. I think the performance will
 * be better, altough I don't know how much optimization is going on for Locks.
 * This needs to be tested.
 *
 * @author Peter Veentjer.
 * @see JucLatch
 */
public class IntrinsicLatch implements Latch {

    private volatile boolean open;

    /**
     * Creates an IntrinsicLatch that is closed.
     */
    public IntrinsicLatch() {
        this(false);
    }

    /**
     * Creates an IntrinsicLatch
     *
     * @param open true if the Latch already is opened, false otherwise.
     */
    public IntrinsicLatch(boolean open) {
        this.open = open;
    }

    public boolean isOpen() {
        return open;
    }

    public void open() {
        if (open)
            return;

        synchronized (this) {
            if (!open) {
                open = true;
                notifyAll();
            }
        }
    }

    public void await() throws InterruptedException {
        if (open)
            return;

        synchronized (this) {
            while (!open)
                wait();
        }
    }

    public void tryAwait(long timeout, TimeUnit unit) throws TimeoutException {
        if (unit == null) throw new NullPointerException();

        if (open)
            return;

        //todo: reminder, calling with a negative timeout doesn't work like expected
        //with the wait method. A negative timeout is interpreted as a endless timeout.
        synchronized (this) {

        }
    }
}
