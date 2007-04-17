/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

/**
 * Unittests the {@link Latch#await()} method.
 *
 * @author Peter Veentjer.
 */
public class Latch_awaitTest extends Latch_AbstractTest {

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
        awaitThread.assertIsTerminated();
        awaitThread.assertIsTerminatedWithInterruptStatus(startInterrupted);
    }

    public void testNotOpen_startInterrupted() {
        newClosedLatch();
        AwaitThread awaitThread = scheduleAwait(START_INTERRUPTED);

        joinAll(awaitThread);
        awaitThread.assertIsInterruptedByException();
    }

    public void testSomeWaitingNeeded() {
        newClosedLatch();
        AwaitThread awaitThread = scheduleAwait(START_UNINTERRUPTED);

        sleepMs(DELAY_SMALL_MS);
        awaitThread.assertIsStarted();

        //now open the latch, and check if the awaitthread was successful.
        OpenThread openThread = scheduleOpen();
        joinAll(awaitThread, openThread);
        awaitThread.assertIsTerminated();
        awaitThread.assertIsTerminatedWithInterruptStatus(false);
    }

    public void testInterruptedWhileWaiting() {
        newClosedLatch();
        AwaitThread awaitThread = scheduleAwait(START_UNINTERRUPTED);

        sleepMs(DELAY_SMALL_MS);
        awaitThread.assertIsStarted();

        awaitThread.interrupt();
        sleepMs(DELAY_SMALL_MS);
        awaitThread.assertIsInterruptedByException();
    }

    public void testSpuriousWakeup() {
        newClosedLatch();
        AwaitThread awaitThread = scheduleAwait(START_UNINTERRUPTED);

        sleepMs(DELAY_SMALL_MS);
        awaitThread.assertIsStarted();

        Thread spuriousThread = scheduleSpuriousWakeup();
        joinAllAndSleepMs(DELAY_SMALL_MS,spuriousThread);
        awaitThread.assertIsStarted();

        OpenThread openThread = scheduleOpen();
        joinAll(awaitThread, openThread);
        awaitThread.assertIsTerminated();
        awaitThread.assertIsTerminatedWithInterruptStatus(false);
    }
}
