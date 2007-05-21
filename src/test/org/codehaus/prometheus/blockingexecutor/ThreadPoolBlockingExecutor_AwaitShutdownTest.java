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
        AwaitShutdownThread waiter1Thread = scheduleAwaitShutdown();
        AwaitShutdownThread waiter2Thread = scheduleAwaitShutdown();

        //make sure that all waiters are waiting
        giveOthersAChance();
        waiter1Thread.assertIsStarted();
        waiter2Thread.assertIsStarted();

        shutdown();

        //make sure that all waiters have terminated without problems
        giveOthersAChance();
        waiter1Thread.assertIsTerminatedWithoutThrowing();
        waiter2Thread.assertIsTerminatedWithoutThrowing();
        assertIsShutdown();
    }


    public void testStarted() {
        newStartedBlockingExecutor(1, 1);
        AwaitShutdownThread waiter1Thread = scheduleAwaitShutdown();
        AwaitShutdownThread waiter2Thread = scheduleAwaitShutdown();

        //make sure that all waiters are waiting
        giveOthersAChance();
        waiter1Thread.assertIsStarted();
        waiter2Thread.assertIsStarted();

        shutdown();

        //make sure that all waiters have terminated without problems
        giveOthersAChance();
        waiter1Thread.assertIsTerminatedWithoutThrowing();
        waiter2Thread.assertIsTerminatedWithoutThrowing();
        assertIsShutdown();
    }

    public void testShuttingDown() {
        newShuttingDownBlockingExecutor(DELAY_MEDIUM_MS);
        AwaitShutdownThread waiter1Thread = scheduleAwaitShutdown();
        AwaitShutdownThread waiter2Thread = scheduleAwaitShutdown();

        //make sure that all waiters are waiting
        giveOthersAChance();
        waiter1Thread.assertIsStarted();
        waiter2Thread.assertIsStarted();

        //wait for the waiters to complete
        joinAll(waiter1Thread, waiter2Thread);
        waiter1Thread.assertIsTerminatedWithoutThrowing();
        waiter2Thread.assertIsTerminatedWithoutThrowing();
        assertIsShutdown();
    }

    public void testShutdown() {
        newShutdownBlockingExecutor(1, 1);
        AwaitShutdownThread waiter1Thread = scheduleAwaitShutdown();
        AwaitShutdownThread waiter2Thread = scheduleAwaitShutdown();

        //make sure all waiters finish without problems
        giveOthersAChance();
        waiter1Thread.assertIsTerminatedWithoutThrowing();
        waiter2Thread.assertIsTerminatedWithoutThrowing();
        assertIsShutdown();
    }

    public void testSomeWaitingNeeded() {
        newStartedBlockingExecutor(1, 1, new NonInterruptableSleepingRunnable(DELAY_LONG_MS));
        AwaitShutdownThread waiter1Thread = scheduleAwaitShutdown();
        AwaitShutdownThread waiter2Thread = scheduleAwaitShutdown();

        //make sure that all waiters are waiting
        giveOthersAChance();
        waiter1Thread.assertIsStarted();
        waiter2Thread.assertIsStarted();

        Thread shutdownThread = scheduleShutdown();

        //wait for the shutdown to complete and make sure that the waiters have completed
        joinAll(shutdownThread, waiter1Thread, waiter2Thread);
        waiter1Thread.assertIsTerminatedWithoutThrowing();
        waiter2Thread.assertIsTerminatedWithoutThrowing();
        assertIsShutdown();
    }

    public void testInterruptedWhileWaiting() {
        newShuttingDownBlockingExecutor(DELAY_EON_MS);

        //make sure that all waiters are waiting
        AwaitShutdownThread waiter1Thread = scheduleAwaitShutdown();
        AwaitShutdownThread waiter2Thread = scheduleAwaitShutdown();
        giveOthersAChance();
        waiter1Thread.assertIsStarted();
        waiter2Thread.assertIsStarted();

        //interrupt waiter1 and make sure that it is terminated and waiter2 is still waiting
        waiter1Thread.interrupt();
        giveOthersAChance(DELAY_MEDIUM_MS);        
        waiter1Thread.assertIsInterruptedByException();
        waiter2Thread.assertIsStarted();
    }

    private void shutdown() {
        ShutdownThread shutdownThread = scheduleShutdown();
        joinAll(shutdownThread);
        shutdownThread.assertIsTerminatedWithoutThrowing();
    }
}
