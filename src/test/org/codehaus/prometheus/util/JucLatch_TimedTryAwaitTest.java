/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.joinAll;
import org.codehaus.prometheus.concurrenttesting.Delays;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unittests the {@link JucLatch#tryAwait(long,TimeUnit)} method.
 *
 * @author Peter Veentjer.
 */
public class JucLatch_TimedTryAwaitTest extends JucLatch_AbstractTest {

    public void testArguments() throws TimeoutException, InterruptedException {
        newOpenLatch();

        try {
            latch.tryAwait(1, null);
            fail();
        } catch (NullPointerException e) {
            assertTrue(true);
        }
    }

    public void testNegativeTimeout() throws InterruptedException {
        newOpenLatch();

        try {
            latch.tryAwait(-1, TimeUnit.MILLISECONDS);
            fail();
        } catch (TimeoutException ex) {
            assertTrue(true);
        }
    }

    public void testNoWaitingNeeded_startInterrupted() {
        testNoWaitingNeeded(START_INTERRUPTED);
    }

    public void testNoWaitingNeeded_startUninterrupted() {
        testNoWaitingNeeded(START_UNINTERRUPTED);
    }

    public void testNoWaitingNeeded(boolean startInterupted) {
        newOpenLatch();

        TimedTryAwaitThread awaitThread = scheduleTimedTryAwait(startInterupted, 0);

        joinAll(awaitThread);
        awaitThread.assertIsTerminatedNormally();
        awaitThread.assertIsTerminatedWithInterruptFlag(startInterupted);
    }

    public void testWaitingNeeded_startInterrupted() {
        newClosedLatch();

        TimedTryAwaitThread awaitThread = scheduleTimedTryAwait(START_INTERRUPTED, 10);

        joinAll(awaitThread);
        awaitThread.assertIsTerminatedByInterruptedException();
    }

    public void testWaitingNeeded_startUninterrupted() {
        newClosedLatch();
        TimedTryAwaitThread awaitThread = scheduleTimedTryAwait(START_UNINTERRUPTED, Delays.EON_MS);

        //make sure the awaitThread is waiting.
        giveOthersAChance();
        awaitThread.assertIsStarted();

        spawned_open();

        joinAll(awaitThread);
        awaitThread.assertIsTerminatedNormally();
        awaitThread.assertIsTerminatedWithInterruptFlag(false);
    }

    public void testTooMuchWaiting() {
        newClosedLatch();
        TimedTryAwaitThread awaitThread = scheduleTimedTryAwait(START_UNINTERRUPTED, Delays.SMALL_MS);

        //let the awaitThread complete, and check that is has timed out.
        joinAll(awaitThread);
        awaitThread.assertIsTimedOut();
    }

    public void testInterruptedWhileWaiting() {
        newClosedLatch();
        TimedTryAwaitThread awaitThread = scheduleTimedTryAwait(START_UNINTERRUPTED, Delays.EON_MS);

        //make sure the awaitThread is waiting.
        giveOthersAChance();
        awaitThread.assertIsStarted();

        //interrupt the awaitThread and make sure it is terminated 
        awaitThread.interrupt();
        joinAll(awaitThread);
        awaitThread.assertIsTerminatedByInterruptedException();
    }

    public void testSpuriousWakeups() {
        newClosedLatch();
        TimedTryAwaitThread awaitThread = scheduleTimedTryAwait(START_UNINTERRUPTED, Delays.EON_MS);

        //make sure the awaitThread is waiting.
        giveOthersAChance();
        awaitThread.assertIsStarted();

        //make sure the awaitThread is waiting after a spurious wakeup
        Thread spuriousThread = scheduleSpuriousWakeup();
        joinAll(spuriousThread);
        giveOthersAChance();
        awaitThread.assertIsStarted();

        //open the latch, and check that the awaitThread has finished successfully.
        spawned_open();
        joinAll(awaitThread);
        awaitThread.assertIsTerminatedNormally();
    }
}
