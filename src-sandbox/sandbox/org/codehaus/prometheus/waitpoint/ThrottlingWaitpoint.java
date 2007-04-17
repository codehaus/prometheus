/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.waitpoint;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A ThrottlingWaitpoint is a Waitpoint that is able to throttle
 * threads that want to passUninterruptibly the waitpoint. This can be done by
 * providing a minimum delay between passages. If this minimal
 * delay is set, messages will have a delay that is equal or bigger
 * than the minimum delay. If you want 100 passages per second, you
 * could set a minimum delay to 1 second / 100 messages =
 * 10 milliseconds/message.
 *
 * @author Peter Veentjer.
 */
public class ThrottlingWaitpoint extends AbstractWaitpoint {

    public static float delayToFrequency(long delay, TimeUnit unit) {
        throw new RuntimeException();
    }

    private volatile float frequency;
    private volatile long periodNs;
    private volatile long nextNs;

    public ThrottlingWaitpoint(float frequency) {
        this.frequency = frequency;
        this.periodNs = (long)(TimeUnit.SECONDS.toNanos(1)/frequency);
        System.out.println("period:"+periodNs);
    }

    public float getFrequency() {
        return frequency;
    }

    public void pass() throws InterruptedException {
        delayIfNeededInterruptibly();
    }


    private void delayIfNeeded() {
        try {
            delayIfNeededInterruptibly();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            //verder delayen???
        }
    }

    private void delayIfNeededInterruptibly() throws InterruptedException {
        long delayNs = calcDelayNs();
        if (delayNs == 0)
            return;

        long delayMs = TimeUnit.NANOSECONDS.toMillis(delayNs);
        //todo: precision loss.
        Thread.sleep(delayMs);
    }

    public synchronized long calcDelayNs() {
        long currentNs = System.nanoTime();
        if(currentNs<nextNs){
            //we zijn er nog niet, we moeten wachten
            long delay = nextNs-currentNs;
            nextNs = currentNs+ periodNs;
            return delay;
        }else{
            nextNs = currentNs+ periodNs;                     
            //we zijn er
            return 0;
        }
    }

    public void passUninterruptibly() {
        delayIfNeeded();
    }

    public boolean isPassible() {
        return calcDelayNs() == 0;
    }

    public long tryPass(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        //calls die niet succesvol zijn verlopen, die moeten niet meedoen in de admin?
        throw new RuntimeException();
    }


    public void enter() throws InterruptedException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void enterInterruptibly() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void tryEnter(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void exit() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
