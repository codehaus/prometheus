package org.codehaus.prometheus.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unittests the {@link Latch#tryAwait(long,TimeUnit)} method.
 *
 * @author Peter Veentjer.
 */
public class Latch_TimedTryAwaitTest extends Latch_AbstractTest {

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
        awaitThread.assertIsTerminatedWithoutThrowing();
        awaitThread.assertIsTerminatedWithInterruptStatus(startInterupted);
    }

    public void testWaitingNeeded_startInterrupted() {
        newClosedLatch();

        TimedTryAwaitThread awaitThread = scheduleTimedTryAwait(START_INTERRUPTED, 10);

        joinAll(awaitThread);
        awaitThread.assertIsInterruptedByException();
    }

    public void testWaitingNeeded_startUninterrupted() {
        newClosedLatch();
        TimedTryAwaitThread awaitThread = scheduleTimedTryAwait(START_UNINTERRUPTED, DELAY_LONG_MS);

        //make sure the awaitThread is waiting.
        giveOthersAChance();
        awaitThread.assertIsStarted();

        //open the latch, and check that the awaitThread has finished successfully. 
        OpenThread openThread = scheduleOpen();
        joinAll(openThread, awaitThread);
        awaitThread.assertIsTerminatedWithoutThrowing();
        awaitThread.assertIsTerminatedWithInterruptStatus(false);
    }

    public void testTooMuchWaiting() {
        newClosedLatch();
        TimedTryAwaitThread awaitThread = scheduleTimedTryAwait(START_UNINTERRUPTED, DELAY_SMALL_MS);

        //let the awaitThread complete, and check that is has timed out.
        joinAll(awaitThread);
        awaitThread.assertIsTimedOut();
        awaitThread.assertIsTerminatedWithInterruptStatus(false);
    }

    public void testInterruptedWhileWaiting() {
        newClosedLatch();
        TimedTryAwaitThread awaitThread = scheduleTimedTryAwait(START_UNINTERRUPTED, DELAY_LONG_MS);

        //make sure the awaitThread is waiting.
        giveOthersAChance();
        awaitThread.assertIsStarted();

        //interrupt the awaitThread 
        awaitThread.interrupt();

        joinAll(awaitThread);
        awaitThread.assertIsInterruptedByException();
    }

    public void testSpuriousWakeups() {
        newClosedLatch();
        TimedTryAwaitThread awaitThread = scheduleTimedTryAwait(START_UNINTERRUPTED, DELAY_LONG_MS);

        //make sure the awaitThread is waiting.
        giveOthersAChance();
        awaitThread.assertIsStarted();

        //make sure the awaitThread is waiting after a spurious wakeup
        Thread spuriousThread = scheduleSpuriousWakeup();
        joinAll(spuriousThread);
        giveOthersAChance();
        awaitThread.assertIsStarted();
                
        //open the latch, and check that the awaitThread has finished successfully.
        OpenThread openThread = scheduleOpen();
        joinAll(openThread, awaitThread);
        awaitThread.assertIsTerminatedWithoutThrowing();
        awaitThread.assertIsTerminatedWithInterruptStatus(false);
    }
}
