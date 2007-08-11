/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.waitpoint;

import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.*;
import org.codehaus.prometheus.testsupport.Delays;

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

        TimedTryPassThread tryPassThread = scheduleTimedTryPass(START_INTERRUPTED, 3 * Delays.SMALL_MS);
        joinAll(tryPassThread);
        tryPassThread.assertIsTerminatedByInterruptedException();
    }

    public void testSomeWaitingNeeded() {
        newClosedCloseableWaitpoint();

        TimedTryPassThread tryPassThread = scheduleTimedTryPass(3 * Delays.SMALL_MS);
        sleepMs(Delays.SMALL_MS);
        tryPassThread.assertIsStarted();

        OpenThread openThread = scheduleOpen();
        joinAll(openThread, tryPassThread);
        tryPassThread.assertSuccess(2 * Delays.SMALL_MS);
    }

    public void testTooMuchWaiting() {
        newClosedCloseableWaitpoint();

        TimedTryPassThread tryPassThread = scheduleTimedTryPass(Delays.SMALL_MS);
        joinAll(tryPassThread);

        tryPassThread.assertIsTimedOut();
    }

    public void testInterruptedWhileWaiting() {
        newClosedCloseableWaitpoint();

        TimedTryPassThread tryPassThread = scheduleTimedTryPass(Delays.LONG_MS);

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

        TimedTryPassThread tryPassThread = scheduleTimedTryPass(4 * Delays.SMALL_MS);
        sleepMs(Delays.SMALL_MS);
        tryPassThread.assertIsStarted();

        Thread spuriousThread = scheduleSpuriousWakeup();
        joinAll(spuriousThread);

        sleepMs(Delays.SMALL_MS);
        tryPassThread.assertIsStarted();

        OpenThread openThread = scheduleOpen();
        joinAll(openThread, tryPassThread);
        tryPassThread.assertSuccess(2 * Delays.SMALL_MS);
    }
}
