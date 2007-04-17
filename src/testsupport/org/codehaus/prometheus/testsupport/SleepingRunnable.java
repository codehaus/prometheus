/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import org.codehaus.prometheus.util.ConcurrencyUtil;

import java.util.concurrent.TimeUnit;

/**
 * A {@link BlockingRunnable} that can sleep for a certain amount of time. The sleep can be
 * interrupted. If you don't want the sleepMs to be interrupted, check the
 * {@link NonInterruptableSleepingRunnable}.
 *
 * @author Peter Veentjer.
 * @see NonInterruptableSleepingRunnable
 */
public class SleepingRunnable extends BlockingRunnable {
    private final long sleep;
    private final TimeUnit sleepUnit;

    /**
     * Creates a new SleepingRunnable that sleeps the given number of
     * milliseconds.
     *
     * @param sleepMs the period to sleepMs in milliseconds.
     */
    public SleepingRunnable(long sleepMs) {
        this(sleepMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a new SleepingRunnable that sleeps the given
     * amount of time.
     *
     * @param sleep
     * @param unit
     * @throws NullPointerException if unit is null.
     */
    public SleepingRunnable(long sleep, TimeUnit unit) {
        if (unit == null) throw new NullPointerException();
        this.sleep = sleep;
        this.sleepUnit = unit;
    }

    /**
     * Returns the period to sleep.
     *
     * @return the period to sleep.
     */
    public long getSleep() {
        return sleep;
    }

    /**
     * Returns the TimeUnit of the period to sleep.
     *
     * @return the TimeUnit of the period to sleep.
     */
    public TimeUnit getSleepUnit() {
        return sleepUnit;
    }

    public void runBlockingInternal() throws InterruptedException {
        //todo: remove dependency
        ConcurrencyUtil.sleep(sleep, sleepUnit);
    }
}
