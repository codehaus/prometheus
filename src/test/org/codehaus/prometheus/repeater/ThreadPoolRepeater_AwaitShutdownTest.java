/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.testsupport.BlockingState;
import org.codehaus.prometheus.testsupport.SleepingRunnable;

/**
 * Unittests the {@link ThreadPoolRepeater#awaitShutdown()} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_AwaitShutdownTest extends ThreadPoolRepeater_AbstractTest {

    public void testNotStarted_strict() throws InterruptedException {
        testNotStarted(true);
    }

    public void testNotStarted_relaxed() throws InterruptedException {
        testNotStarted(false);
    }

    public void testNotStarted(boolean strict) throws InterruptedException {
        newUnstartedRepeater(strict);
        assertAwaitForTerminationSucceedsWhenShutdown();
    }

    //=========================================================

    public void testStartedButNoJob_strict() {
        testStartedButNoJob(true);
    }

    public void testStartedButNoJob_relaxed() {
        testStartedButNoJob(false);
    }

    public void testStartedButNoJob(boolean strict) {
        newRunningRepeater(strict);
        assertAwaitForTerminationSucceedsWhenShutdown();
    }

    //=========================================================

    public void testStartedAndRunningJob_strict() {
        testStartedAndRunningJob(true);
    }

    public void testStartedAndRunningJob_relaxed() {
        testStartedAndRunningJob(false);
    }

    public void testStartedAndRunningJob(boolean strict) {
        newRunningRepeater(strict, new RepeatableRunnable(new SleepingRunnable(DELAY_SMALL_MS)));
        assertAwaitForTerminationSucceedsWhenShutdown();
    }

    //=========================================================

    public void testShuttingDown_strict() throws InterruptedException {
        testShuttingDown(true);
    }

    public void testShuttingDown_relaxed() throws InterruptedException {
        testShuttingDown(false);
    }

    public void testShuttingDown(boolean strict) throws InterruptedException {
        newShuttingdownRepeater(strict, 2 * DELAY_SMALL_MS);
        assertAwaitForTerminationSucceedsWhenShutdown();
    }

    //=========================================================

    public void testShutdown_strict() throws InterruptedException {
        testShutdown(true);
    }

    public void testShutdown_relaxed() throws InterruptedException {
        testShutdown(false);
    }

    public void testShutdown(boolean strict) throws InterruptedException {
        newShutdownRepeater(strict);

        AwaitShutdownThread t1 = scheduleAwaitSchutdown();
        AwaitShutdownThread t2 = scheduleAwaitSchutdown();

        joinAll(t1, t2);

        t1.assertIsFinished();
        t2.assertIsFinished();
    }

    //=========================================================

    public void testInterruptedWhileWaiting_strict() {
        testInterruptedWhileWaiting(true);
    }

    public void testInterruptedWhileWaiting_relaxed() {
        testInterruptedWhileWaiting(false);
    }

    public void testInterruptedWhileWaiting(boolean strict) {
        newShuttingdownRepeater(strict, DELAY_MEDIUM_MS);

        AwaitShutdownThread awaitThread = scheduleAwaitSchutdown();

        //make sure it is waiting
        sleepMs(DELAY_TINY_MS);
        awaitThread.assertIsWaiting();

        //now interrupt the awaitThread
        awaitThread.interrupt();
        joinAll(awaitThread);
        awaitThread.assertIsInterrupted();
        assertIsShuttingdown();
    }

    //=========================================================

    public AwaitShutdownThread scheduleAwaitSchutdown() {
        AwaitShutdownThread t = new AwaitShutdownThread();
        t.start();
        return t;
    }

    //=========================================================

    public void assertAwaitForTerminationSucceedsWhenShutdown() {
        AwaitShutdownThread t1 = scheduleAwaitSchutdown();
        AwaitShutdownThread t2 = scheduleAwaitSchutdown();

        repeater.shutdownNow();

        joinAll(t1, t2);

        t1.assertIsFinished();
        t2.assertIsFinished();
    }

    //=========================================================

    class AwaitShutdownThread extends Thread {
        volatile BlockingState state;

        public void run() {
            state = BlockingState.waiting;
            try {
                repeater.awaitShutdown();
                state = BlockingState.finished;
            } catch (InterruptedException e) {
                state = BlockingState.interrupted;
            }
        }

        public void assertIsInterrupted() {
            assertEquals(BlockingState.interrupted, state);
        }

        public void assertIsFinished() {
            assertEquals(BlockingState.finished, state);
        }

        public void assertIsWaiting() {
            assertEquals(BlockingState.waiting, state);
        }
    }
}

