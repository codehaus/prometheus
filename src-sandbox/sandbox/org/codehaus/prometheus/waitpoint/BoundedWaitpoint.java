/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.waitpoint;

import static org.codehaus.prometheus.util.ConcurrencyUtil.toUsableNanos;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The BoundedWaitpoint maintains a pair of associated Waitpoints:
 * <ol>
 * <li>
 * <b>frontWaitpoint</b>: if an item needs to be placed, the frontWaitpoint
 * needs to be passed. If there is space available, the size is increased
 * and the passUninterruptibly succeeds. If there is no space available, the passUninterruptibly
 * block until space comes available.
 * </li>
 * <li>
 * <b>backWaitpoint</b>: if an item needs to be removed, the backWaitpoint
 * needs to be passed. If there is an item available, the size is decreased
 * and the passUninterruptibly succeeds. If there is no item available, the passUninterruptibly
 * blocks until an item comes available.
 * </li>
 * </ol>
 * <p/>
 * The BoundedWaitpoint can be used to make a structure 'bounded': you can
 * control the number of items
 *
 * @author Peter Veentjer.
 */
public class BoundedWaitpoint implements Waitsection {

    private final Semaphore semaphore;

    public BoundedWaitpoint(Semaphore semaphore) {
        if (semaphore == null) throw new NullPointerException();
        this.semaphore = semaphore;
    }

    public void enter() throws InterruptedException {
        semaphore.acquire();
    }

    public void enterUninterruptibly() {
        semaphore.acquireUninterruptibly();
    }

    public boolean isEnterable() {
        return semaphore.availablePermits() > 0;
    }

    public boolean tryEnter() {
        return semaphore.tryAcquire();
    }

    public long tryEnter(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        long startNs = System.nanoTime();

        if (!semaphore.tryAcquire(timeout, unit))
            throw new TimeoutException();

        return System.nanoTime() - startNs;
    }

    public long tryEnterUninterruptibly(long timeout, TimeUnit unit) throws TimeoutException {
        long timeoutNs = toUsableNanos(timeout, unit);

        try {
            long startNs = System.nanoTime();
            if (!semaphore.tryAcquire(timeoutNs, unit))
                throw new TimeoutException();

            return System.nanoTime() - startNs;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TimeoutException();
        }
    }

    public void exit() {
        semaphore.release();
    }
}
