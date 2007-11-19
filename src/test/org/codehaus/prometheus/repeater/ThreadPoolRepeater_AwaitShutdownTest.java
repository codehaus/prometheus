/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import static org.codehaus.prometheus.concurrenttesting.TestSupport.newSleepingRunnable;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.joinAll;
import org.codehaus.prometheus.concurrenttesting.Delays;

/**
 * Unittests the {@link ThreadPoolRepeater#awaitShutdown()} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_AwaitShutdownTest extends ThreadPoolRepeater_AbstractTest {

//=========================================================

    public void testWhileUnstarted_strict() throws InterruptedException {
        testWhileUnstarted(true);
    }

    public void testWhileUnstarted_relaxed() throws InterruptedException {
        testWhileUnstarted(false);
    }

    public void testWhileUnstarted(boolean strict) throws InterruptedException {
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

    //====================running=====================================

    public void testStartedAndRunningJob_strict() {
        testStartedAndRunningJob(true);
    }

    public void testStartedAndRunningJob_relaxed() {
        testStartedAndRunningJob(false);
    }

    public void testStartedAndRunningJob(boolean strict) {
        newRunningRepeater(strict, new RepeatableRunnable(newSleepingRunnable(Delays.SMALL_MS)),1);
        assertAwaitForTerminationSucceedsWhenShutdown();
    }

    //=================shuttingdownnormally========================================

    public void testShuttingDown_strict() throws InterruptedException {
        testShuttingDown(true);
    }

    public void testShuttingDown_relaxed() throws InterruptedException {
        testShuttingDown(false);
    }

    public void testShuttingDown(boolean strict) throws InterruptedException {
        newShuttingdownRepeater(strict, 2 * Delays.SMALL_MS);
        assertAwaitForTerminationSucceedsWhenShutdown();
    }

    //==============shuttingdownforced================================

    public void testForcedShuttingdown(){
        newForcedShuttingdownRepeater(Delays.LONG_MS,1);

        assertAwaitForTerminationSucceedsWhenShutdown();
    }

    //=========================================================

    public void testWhileShutdown_strict() throws InterruptedException {
        testShutdown(true);
    }

    public void testWhileShutdown_relaxed() throws InterruptedException {
        testShutdown(false);
    }

    public void testShutdown(boolean strict) throws InterruptedException {
        newShutdownRepeater(strict);

        AwaitShutdownThread t1 = scheduleAwaitSchutdown();
        AwaitShutdownThread t2 = scheduleAwaitSchutdown();

        joinAll(t1, t2);

        t1.assertIsTerminatedNormally();
        t2.assertIsTerminatedNormally();
    }

    //=========================================================

    public void testInterruptedWhileWaiting_strict() {
        testInterruptedWhileWaiting(true);
    }

    public void testInterruptedWhileWaiting_relaxed() {
        testInterruptedWhileWaiting(false);
    }

    public void testInterruptedWhileWaiting(boolean strict) {
        newShuttingdownRepeater(strict, Delays.MEDIUM_MS);

        AwaitShutdownThread awaitThread = scheduleAwaitSchutdown();
        //make sure it is waiting
        giveOthersAChance();
        awaitThread.assertIsStarted();

        //now interrupt the awaitThread
        awaitThread.interrupt();
        joinAll(awaitThread);
        awaitThread.assertIsTerminatedByInterruptedException();
        assertIsShuttingdown();
    }

    //=========================================================

    //=========================================================

    public void assertAwaitForTerminationSucceedsWhenShutdown() {
        AwaitShutdownThread t1 = scheduleAwaitSchutdown();
        AwaitShutdownThread t2 = scheduleAwaitSchutdown();

        repeater.shutdownNow();

        joinAll(t1, t2);

        t1.assertIsTerminatedNormally();
        t2.assertIsTerminatedNormally();
    }
}

