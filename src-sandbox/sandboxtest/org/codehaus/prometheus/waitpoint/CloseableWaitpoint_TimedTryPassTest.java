/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.waitpoint;

import static org.codehaus.prometheus.testsupport.TestUtil.sleepMs;
import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;

/**
 * Unittests the {@link org.codehaus.prometheus.waitpoint.CloseableWaitpoint#tryPass(long,java.util.concurrent.TimeUnit)}
 * method.
 *
 * @author Peter Veentjer.
 */
public class CloseableWaitpoint_TimedTryPassTest extends CloseableWaitpoint_AbstractTest {

    public void testNoWaitingNeeded_startUninterrupted() {
        testNoWaitingNeeded(START_UNINTERRUPTED);
    }

    public void testNoWaitingNeeded_startInterrupted() {
        testNoWaitingNeeded(START_INTERRUPTED);
    }

    public void testNoWaitingNeeded(boolean startInterrupted) {
        newOpenCloseableWaitpoint();
        TimedTryPassThread t = scheduleTimedTryPass(startInterrupted, 0);
        joinAll(t);

        t.assertSuccess(0);
    }

    public void testSomeWaitingNeeded_startInterrupted() {
        newClosedCloseableWaitpoint();

        TimedTryPassThread tryPassThread = scheduleTimedTryPass(START_INTERRUPTED, 3 * DELAY_SMALL_MS);
        joinAll(tryPassThread);
        tryPassThread.assertIsTerminatedByInterruptedException();
    }

    public void testSomeWaitingNeeded() {
        newClosedCloseableWaitpoint();

        TimedTryPassThread tryPassThread = scheduleTimedTryPass(3 * DELAY_SMALL_MS);
        sleepMs(DELAY_SMALL_MS);
        tryPassThread.assertIsStarted();

        OpenThread openThread = scheduleOpen();
        joinAll(openThread, tryPassThread);
        tryPassThread.assertSuccess(2 * DELAY_SMALL_MS);
    }

    public void testTooMuchWaiting() {
        newClosedCloseableWaitpoint();

        TimedTryPassThread tryPassThread = scheduleTimedTryPass(DELAY_SMALL_MS);
        joinAll(tryPassThread);

        tryPassThread.assertIsTimedOut();
    }

    public void testInterruptedWhileWaiting() {
        newClosedCloseableWaitpoint();

        TimedTryPassThread tryPassThread = scheduleTimedTryPass(DELAY_LONG_MS);

        //make sure the thread is waiting.
        giveOthersAChance();
        tryPassThread.assertIsStarted();

        //interrupt the tryPass and make check that the thread was interrupted
        tryPassThread.interrupt();
        giveOthersAChance();
        tryPassThread.assertIsTerminatedByInterruptedException();
    }

    public void testSpuriousWakeups() {
        newClosedCloseableWaitpoint();

        TimedTryPassThread tryPassThread = scheduleTimedTryPass(4 * DELAY_SMALL_MS);
        sleepMs(DELAY_SMALL_MS);
        tryPassThread.assertIsStarted();

        Thread spuriousThread = scheduleSpuriousWakeup();
        joinAll(spuriousThread);

        sleepMs(DELAY_SMALL_MS);
        tryPassThread.assertIsStarted();

        OpenThread openThread = scheduleOpen();
        joinAll(openThread, tryPassThread);
        tryPassThread.assertSuccess(2 * DELAY_SMALL_MS);
    }
}
