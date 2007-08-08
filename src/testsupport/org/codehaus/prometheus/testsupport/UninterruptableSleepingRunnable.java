/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import static org.codehaus.prometheus.util.ConcurrencyUtil.sleepUninterruptibly;

import java.util.concurrent.TimeUnit;

/**
 * A {@link BlockingRunnable} that sleeps uninterruptibly for
 * a certain amount of time. If you want the sleepMsOld to be interruptible,
 * check out the {@link SleepingRunnable}.
 *
 * @author Peter Veentjer.
 * @see org.codehaus.prometheus.testsupport.SleepingRunnable
 */
public class UninterruptableSleepingRunnable extends BlockingRunnable {

    private final long sleep;
    private final TimeUnit unit;

    /**
     * Creates a new UninterruptableSleepingRunnable that sleeps for
     * the given number of milliseconds.
     *
     * @param sleepMs the period to sleepMsOld in milliseconds.
     */
    public UninterruptableSleepingRunnable(long sleepMs) {
        this(sleepMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a new UninterruptableSleepingRunnable that sleeps for
     * the given amount of time.
     *
     * @param sleep
     * @param unit
     */
    public UninterruptableSleepingRunnable(long sleep, TimeUnit unit) {
        this.sleep = sleep;
        this.unit = unit;
    }

    public void runBlockingInternal() {
        sleepUninterruptibly(sleep, unit);
    }
}
