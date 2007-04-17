/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import org.codehaus.prometheus.util.ConcurrencyUtil;

import java.util.concurrent.TimeUnit;

/**
 * A {@link BlockingRunnable} that sleeps uninterruptibly for
 * a certain amount of time. If you want the sleepMs to be interruptible,
 * check out the {@link SleepingRunnable}.
 *
 * @author Peter Veentjer.
 * @see org.codehaus.prometheus.testsupport.SleepingRunnable
 */
public class NonInterruptableSleepingRunnable extends BlockingRunnable {

    private final long sleep;
    private final TimeUnit unit;

    /**
     * Creates a new NonInterruptableSleepingRunnable that sleeps for
     * the given number of milliseconds.
     *
     * @param sleepMs the period to sleepMs in milliseconds.
     */
    public NonInterruptableSleepingRunnable(long sleepMs) {
        this(sleepMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a new NonInterruptableSleepingRunnable that sleeps for
     * the given amount of time.
     *
     * @param sleep
     * @param unit
     */
    public NonInterruptableSleepingRunnable(long sleep, TimeUnit unit) {
        this.sleep = sleep;
        this.unit = unit;
    }

    public void runBlockingInternal() {
        //todo: remove dependency
        ConcurrencyUtil.sleepUninterruptibly(sleep, unit);
    }
}
