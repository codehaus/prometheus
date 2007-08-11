/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import static org.codehaus.prometheus.testsupport.TestSupport.newSleepingRunnable;
import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.testsupport.Delays;
import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.joinAll;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unittests the {@link ThreadPoolRepeater#tryAwaitShutdown(long,TimeUnit)} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_TryAwaitShutdownTest extends ThreadPoolRepeater_AbstractTest {

    public void testArguments() throws TimeoutException, InterruptedException {
        newRunningStrictRepeater();

        try {
            repeater.tryAwaitShutdown(1, null);
            fail();
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testWhileUnstarted() throws InterruptedException {
        newUnstartedStrictRepeater();
        assertAwaitForTerminationSucceedsWhenShutdown(Delays.SMALL_MS);
    }

    public void testWhileRunning_noJob() {
        newRunningStrictRepeater();
        assertAwaitForTerminationSucceedsWhenShutdown(Delays.SMALL_MS);
    }

    public void testRunning_hasJob() {
        newRunningStrictRepeater(new RepeatableRunnable(newSleepingRunnable(Delays.SMALL_MS)));
        assertAwaitForTerminationSucceedsWhenShutdown(Delays.MEDIUM_MS);
    }

    public void testTimeout() {
        newRunningStrictRepeater(new RepeatableRunnable(newSleepingRunnable(Delays.SMALL_MS)));

        TryAwaitShutdownThread t = scheduleTryAwaitShutdown(1);
        joinAll(t);
        t.assertIsTimedOut();
    }

    public void testNegativeTimeout() {
        newShutdownRepeater();

        TryAwaitShutdownThread t = scheduleTryAwaitShutdown(-1);
        joinAll(t);
        t.assertIsTimedOut();
    }

    public void testWhileShuttingdown() throws InterruptedException {
        newShuttingdownRepeater(Delays.MEDIUM_MS);
        assertAwaitForTerminationSucceedsWhenShutdown(Delays.EON_MS);
    }

    public void testWhileForcedShuttingdown(){
        newForcedShuttingdownRepeater(Delays.MEDIUM_MS,10);
        assertAwaitForTerminationSucceedsWhenShutdown(Delays.EON_MS);
    }

    public void testWhileShutdown() throws InterruptedException {
        newShutdownRepeater();

        TryAwaitShutdownThread t1 = scheduleTryAwaitShutdown(0);
        TryAwaitShutdownThread t2 = scheduleTryAwaitShutdown(0);

        joinAll(t1, t2);

        t1.assertIsTerminatedNormally();
        t2.assertIsTerminatedNormally();
    }

    public void testInterruptedWhileWaiting() {
        newRunningStrictRepeater(new RepeatableRunnable(newSleepingRunnable(Delays.MEDIUM_MS)));

        TryAwaitShutdownThread awaitThread = scheduleTryAwaitShutdown(Delays.LONG_MS);
        giveOthersAChance();
        awaitThread.assertIsStarted();

        awaitThread.interrupt();
        joinAll(awaitThread);

        awaitThread.assertIsTerminatedByInterruptedException();
        assertIsRunning();
    }

    public void assertAwaitForTerminationSucceedsWhenShutdown(long timeoutMs) {
        TryAwaitShutdownThread t1 = scheduleTryAwaitShutdown(timeoutMs);
        TryAwaitShutdownThread t2 = scheduleTryAwaitShutdown(timeoutMs);

        giveOthersAChance();

        repeater.shutdownNow();

        joinAll(t1, t2);

        t1.assertIsTerminatedNormally();
        t2.assertIsTerminatedNormally();
    }

    public TryAwaitShutdownThread scheduleTryAwaitShutdown(long timeoutMs) {
        TryAwaitShutdownThread t = new TryAwaitShutdownThread(timeoutMs, TimeUnit.MILLISECONDS);
        t.start();
        return t;
    }

    class TryAwaitShutdownThread extends TestThread {
        private final long timeout;
        private final TimeUnit unit;

        public TryAwaitShutdownThread(long timeout, TimeUnit unit) {
            this.timeout = timeout;
            this.unit = unit;
        }

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            repeater.tryAwaitShutdown(timeout, unit);
            assertIsShutdown();//todo: fixen
        }
    }
}
