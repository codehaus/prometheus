/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.testsupport.TestThread;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConcurrencyUtil_SleepTest extends ConcurrentTestCase {

    public void testArguments() throws InterruptedException {
        try {
            ConcurrencyUtil.sleep(1, null);
            fail();
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testNegativeTimeout() throws InterruptedException {
        stopwatch.start();
        ConcurrencyUtil.sleep(-1, TimeUnit.SECONDS);
        stopwatch.stop();

        stopwatch.assertElapsedSmallerThanMs(100);
    }

    public void testSomeWaiting() throws InterruptedException {
        stopwatch.start();
        SleepThread t = scheduleSleep(2, TimeUnit.SECONDS, false);
        joinAll(t);
        stopwatch.stop();

        stopwatch.assertElapsedBiggerOrEqualThanMs(2000);
        stopwatch.assertElapsedSmallerThanMs(2100);
    }

    public void testInterruptedWhileSleeping() {
        stopwatch.start();
        SleepThread t = scheduleSleep(10, TimeUnit.SECONDS, false);
        Thread.yield();
        t.assertIsStarted();
        t.interrupt();

        joinAll(t);
        stopwatch.stop();

        stopwatch.assertElapsedSmallerThanMs(300);
    }

    public SleepThread scheduleSleep(long period, TimeUnit periodUnit, boolean startInterrupted) {
        SleepThread t = new SleepThread(period, periodUnit);
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public class SleepThread extends TestThread {
        private final long period;
        private final TimeUnit periodUnit;

        public SleepThread(long period, TimeUnit periodUnit) {
            this.period = period;
            this.periodUnit = periodUnit;
        }

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            ConcurrencyUtil.sleep(period, periodUnit);
        }
    }
}
