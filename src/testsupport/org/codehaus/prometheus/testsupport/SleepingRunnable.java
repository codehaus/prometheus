package org.codehaus.prometheus.testsupport;

import static org.codehaus.prometheus.util.ConcurrencyUtil.sleep;
import static org.codehaus.prometheus.util.ConcurrencyUtil.sleepUninterruptibly;

import java.util.concurrent.TimeUnit;

/**
 * @author Peter Veentjer.
 */
public class SleepingRunnable extends BlockingRunnable {

    private final long period;
    private final TimeUnit unit;
    private final boolean interruptible;

    /**
     * Creates a new UninterruptableSleepingRunnable that sleeps for
     * the given amount of time.
     *
     * @param period
     * @param unit
     * @param interruptible if the period is interruptible
     */
    public SleepingRunnable(long period, TimeUnit unit, boolean interruptible) {
        this.period = period;
        this.unit = unit;
        this.interruptible = interruptible;
    }

    /**
     * If the sleeping is interruptible,
     *
     * @return true if the sleeping is interruptible, false otherwise.
     */
    public boolean isInterruptible() {
        return interruptible;
    }

    /**
     * How long to sleep.
     *
     * @return how long to sleep.
     */
    public long getPeriod() {
        return period;
    }

    /**
     * Returns the TimeUnit how to interpret the period.
     *
     * @return the TimeUnit how to interpret the period.
     */
    public TimeUnit getUnit() {
        return unit;
    }

    public void runBlockingInternal() throws InterruptedException {
        if (interruptible)
            sleep(period, unit);
        else
            sleepUninterruptibly(period, unit);
    }
}
