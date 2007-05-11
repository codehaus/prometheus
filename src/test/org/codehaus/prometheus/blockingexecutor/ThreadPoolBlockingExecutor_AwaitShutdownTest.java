/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.testsupport.NonInterruptableSleepingRunnable;

/**
 * Unittests {@link ThreadPoolBlockingExecutor#awaitShutdown()}.
 *
 * @author Peter Veentjer
 */
public class ThreadPoolBlockingExecutor_AwaitShutdownTest extends ThreadPoolBlockingExecutor_AbstractTest {

    public void testNotStarted() {
        newUnstartedBlockingExecutor(1, 1);
        AwaitShutdownThread t1 = scheduleAwaitShutdown();
        AwaitShutdownThread t2 = scheduleAwaitShutdown();

        giveOthersAChance();
        t1.assertIsStarted();
        t2.assertIsStarted();

        shutdown();

        giveOthersAChance();
        t1.assertIsTerminated();
        t2.assertIsTerminated();
        assertIsShutdown();
    }


    public void testStarted() {
        newStartedBlockingExecutor(1, 1);
        AwaitShutdownThread t1 = scheduleAwaitShutdown();
        AwaitShutdownThread t2 = scheduleAwaitShutdown();

        giveOthersAChance();
        t1.assertIsStarted();
        t2.assertIsStarted();

        shutdown();

        giveOthersAChance();
        t1.assertIsTerminated();
        t2.assertIsTerminated();
        assertIsShutdown();
    }

    public void testShuttingDown() {
        newShuttingDownBlockingExecutor(DELAY_MEDIUM_MS);
        AwaitShutdownThread t1 = scheduleAwaitShutdown();
        AwaitShutdownThread t2 = scheduleAwaitShutdown();

        giveOthersAChance();
        t1.assertIsStarted();
        t2.assertIsStarted();

        joinAll(t1, t2);
        t1.assertIsTerminatedWithoutThrowing();
        t2.assertIsTerminatedWithoutThrowing();
        assertIsShutdown();
    }

    public void testShutdown() {
        newShutdownBlockingExecutor(1, 1);
        AwaitShutdownThread t1 = scheduleAwaitShutdown();
        AwaitShutdownThread t2 = scheduleAwaitShutdown();

        giveOthersAChance();
        t1.assertIsTerminated();
        t2.assertIsTerminated();
        assertIsShutdown();
    }

    public void testSomeWaitingNeeded() {
        newStartedBlockingExecutor(1, 1, new NonInterruptableSleepingRunnable(DELAY_LONG_MS));
        AwaitShutdownThread t1 = scheduleAwaitShutdown();
        AwaitShutdownThread t2 = scheduleAwaitShutdown();

        giveOthersAChance();
        t1.assertIsStarted();
        t2.assertIsStarted();

        Thread shutdownThread = scheduleShutdown();
        joinAll(shutdownThread,t1,t2);
        
        t1.assertIsTerminatedWithoutThrowing();
        t2.assertIsTerminatedWithoutThrowing();
        assertIsShutdown();
    }

    public void testInterruptedWhileWaiting() {
        newShuttingDownBlockingExecutor(DELAY_EON_MS);

        AwaitShutdownThread awaitThread = scheduleAwaitShutdown();

        giveOthersAChance();
        awaitThread.assertIsStarted();

        awaitThread.interrupt();
        joinAll(awaitThread);        
        awaitThread.assertIsInterruptedByException();
    }

    private void shutdown() {
        ShutdownThread shutdownThread = scheduleShutdown();
        joinAll(shutdownThread);
        shutdownThread.assertIsTerminatedWithoutThrowing();
    }
}
