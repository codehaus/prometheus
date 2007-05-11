/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;


import org.codehaus.prometheus.testsupport.SleepingRunnable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unittests the {@link ThreadPoolBlockingExecutor#tryAwaitShutdown(long,TimeUnit)} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor_TryAwaitShutdownTest extends ThreadPoolBlockingExecutor_AbstractTest {

    public void testArguments() throws TimeoutException, InterruptedException {
        newStartedBlockingExecutor();
        try {
            executor.tryAwaitShutdown(1, null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testNegativeTimeout() throws InterruptedException {
        newStartedBlockingExecutor();
        try {
            executor.tryAwaitShutdown(-1, TimeUnit.SECONDS);
            fail("TimeoutException expected");
        } catch (TimeoutException e) {
            assertTrue(true);
        }
    }

    public void testTooMuchWaiting() {
        newStartedBlockingExecutor(1, 1, new SleepingRunnable(DELAY_EON_MS));

        TryAwaitShutdownThread awaitThread1 = scheduleTryAwaitShutdown(DELAY_MEDIUM_MS);
        TryAwaitShutdownThread awaitThread2 = scheduleTryAwaitShutdown(DELAY_MEDIUM_MS);
        joinAll(awaitThread1, awaitThread2);

        awaitThread1.assertIsTimedOut();
        awaitThread2.assertIsTimedOut();
        assertIsRunning();
    }

    public void testNotStarted() {
        newUnstartedBlockingExecutor(10, 10);
        assertShutdownNowNotifiesWaiters();
    }

    private void assertShutdownNowNotifiesWaiters() {
        TryAwaitShutdownThread awaitThread1 = scheduleTryAwaitShutdown(DELAY_EON_MS);
        TryAwaitShutdownThread awaitThread2 = scheduleTryAwaitShutdown(DELAY_EON_MS);

        giveOthersAChance();
        awaitThread1.assertIsStarted();
        awaitThread2.assertIsStarted();

        ShutdownNowThread shutdownThread = scheduleShutdownNow();
        joinAll(shutdownThread);
        giveOthersAChance();

        shutdownThread.assertIsTerminatedWithoutThrowing();
        awaitThread1.assertIsTerminatedWithoutThrowing();
        awaitThread2.assertIsTerminatedWithoutThrowing();
        assertIsShutdown();
    }

    //todo: test with shutdown & test with shutdownnow

    public void testStarted_noWorkers() {
        newStartedBlockingExecutor(1, 0);
        assertShutdownNowNotifiesWaiters();
    }

    public void testStarted_IdleWorkers() {
        newStartedBlockingExecutor(1, 10);
        assertShutdownNowNotifiesWaiters();
    }

    public void testStarted_nonIdleWorkers() {
        int poolsize = 10;
        newStartedBlockingExecutor(1, poolsize);
        executeEonTask(poolsize);

        assertShutdownNowNotifiesWaiters();
    }

    //todo:
    public void testShuttingDown() {        
    }

    public void testShutdown() throws InterruptedException {
        newShutdownBlockingExecutor(1, 1);

        TryAwaitShutdownThread awaitThread = scheduleTryAwaitShutdown(0);
        joinAll(awaitThread);

        awaitThread.assertSuccess();
        assertIsShutdown();
    }

    //todo
    public void testInterruptedWhileWaiting() {
    }
}

