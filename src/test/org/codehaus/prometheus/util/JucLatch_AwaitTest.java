/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.joinAll;

/**
 * Unittests the {@link JucLatch#await()} method.
 *
 * @author Peter Veentjer.
 */
public class JucLatch_AwaitTest extends JucLatch_AbstractTest {

    public void testAlreadyOpen_startUninterrupted() {
        testAlreadyOpen(false);
    }

    public void testAlreadyOpen_startInterrupted() {
        testAlreadyOpen(true);
    }

    public void testAlreadyOpen(boolean startInterrupted) {
        newOpenLatch();

        AwaitThread awaitThread = scheduleAwait(startInterrupted);

        joinAll(awaitThread);
        awaitThread.assertIsTerminatedNormally();
        awaitThread.assertIsTerminatedWithInterruptFlag(startInterrupted);
    }

    //============== some waiting needed =========================

    public void testNotOpen_startInterrupted() {
        newClosedLatch();
        AwaitThread awaitThread = scheduleAwait(START_INTERRUPTED);

        joinAll(awaitThread);
        awaitThread.assertIsTerminatedByInterruptedException();
    }

    public void testSomeWaitingNeeded() {
        newClosedLatch();
        AwaitThread awaitThread = scheduleAwait(START_UNINTERRUPTED);

        giveOthersAChance();
        awaitThread.assertIsStarted();

        //now open the latch, and check if the awaitthread was successful.
        OpenThread openThread = scheduleOpen();

        joinAll(awaitThread, openThread);
        awaitThread.assertIsTerminatedNormally();
        awaitThread.assertIsTerminatedWithInterruptFlag(false);
    }

    public void testInterruptedWhileWaiting() {
        newClosedLatch();
        AwaitThread awaitThread = scheduleAwait(START_UNINTERRUPTED);

        giveOthersAChance();
        awaitThread.assertIsStarted();

        awaitThread.interrupt();

        giveOthersAChance();
        awaitThread.assertIsTerminatedByInterruptedException();
    }

    public void testSpuriousWakeup() {
        newClosedLatch();
        AwaitThread awaitThread = scheduleAwait(START_UNINTERRUPTED);

        giveOthersAChance();
        awaitThread.assertIsStarted();

        Thread spuriousThread = scheduleSpuriousWakeup();

        joinAll(spuriousThread);
        giveOthersAChance();
        awaitThread.assertIsStarted();

        OpenThread openThread = scheduleOpen();

        joinAll(awaitThread, openThread);
        awaitThread.assertIsTerminatedNormally();
        awaitThread.assertIsTerminatedWithInterruptFlag(false);
    }
}
