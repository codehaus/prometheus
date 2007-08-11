/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;


import static org.codehaus.prometheus.testsupport.TestSupport.newEonSleepingRunnable;
import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.joinAll;
import org.codehaus.prometheus.testsupport.Delays;

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
        newStartedBlockingExecutor(0, 1, newEonSleepingRunnable());

        TryAwaitShutdownThread awaitThread1 = scheduleTryAwaitShutdown(Delays.MEDIUM_MS);
        TryAwaitShutdownThread awaitThread2 = scheduleTryAwaitShutdown(Delays.MEDIUM_MS);
        joinAll(awaitThread1, awaitThread2);

        awaitThread1.assertIsTimedOut();
        awaitThread2.assertIsTimedOut();
        assertIsRunning();
    }

    private void assertShutdownNowNotifiesWaiters() {
        TryAwaitShutdownThread awaitThread1 = scheduleTryAwaitShutdown(Delays.EON_MS);
        TryAwaitShutdownThread awaitThread2 = scheduleTryAwaitShutdown(Delays.EON_MS);

        giveOthersAChance();
        awaitThread1.assertIsStarted();
        awaitThread2.assertIsStarted();

        ShutdownNowThread shutdownThread = scheduleShutdownNow();
        joinAll(shutdownThread);
        giveOthersAChance();

        shutdownThread.assertIsTerminatedNormally();
        awaitThread1.assertIsTerminatedNormally();
        awaitThread2.assertIsTerminatedNormally();
        assertIsShutdown();
    }


    public void testWhileUnstarted() {
        newUnstartedBlockingExecutor(10, 10);
        assertShutdownNowNotifiesWaiters();
    }


    //todo: test with shutdown & test with shutdownnow

    public void testWhileRunning_noWorkers() {
        newStartedBlockingExecutor(1, 0);
        assertShutdownNowNotifiesWaiters();
    }

    public void testWhileRunning_IdleWorkers() {
        newStartedBlockingExecutor(1, 10);
        assertShutdownNowNotifiesWaiters();
    }

    public void testWhileRunning_nonIdleWorkers() {
        int poolsize = 10;
        newStartedBlockingExecutor(1, poolsize);
        executeEonTask(poolsize);

        assertShutdownNowNotifiesWaiters();
    }

    public void testInterruptedWhileWaiting() {
        newStartedBlockingExecutor();

        TryAwaitShutdownThread awaitThread = scheduleTryAwaitShutdown(Delays.EON_MS);
        giveOthersAChance();
        assertIsRunning();
        awaitThread.assertIsStarted();

        awaitThread.interrupt();
        giveOthersAChance();
        awaitThread.assertIsTerminatedByInterruptedException();
        assertIsRunning();
    }

    //================ shutting down ============================

    public void testWhileShuttingdown_startInterrupted() {
        newShuttingdownBlockingExecutor(Delays.LONG_MS);

        TryAwaitShutdownThread awaitThread = scheduleTryAwaitShutdown(Delays.EON_MS, START_INTERRUPTED);
        giveOthersAChance();
        awaitThread.assertIsTerminatedByInterruptedException();
        assertIsShuttingdown();
    }

    public void testWhileShuttingdown_startUninterrupted() {
        newShuttingdownBlockingExecutor(Delays.LONG_MS);

        TryAwaitShutdownThread awaitThread = scheduleTryAwaitShutdown(Delays.EON_MS, START_UNINTERRUPTED);
        giveOthersAChance();
        awaitThread.assertIsStarted();
        assertIsShuttingdown();

        joinAll(awaitThread);
        awaitThread.assertSuccess();
        awaitThread.assertIsTerminatedWithInterruptFlag(false);
        assertIsShutdown();
    }

    public void testWhileForcedShuttingdown(){
        //todo
    }

    //================ shutdown  ==================================

    public void testWhileShutdown_startInterrupted() {
        testWhileShutdown(START_INTERRUPTED);
    }

    public void testWhileShutdown_startUninterrupted() {
        testWhileShutdown(START_UNINTERRUPTED);
    }

    public void testWhileShutdown(boolean startInterrupted) {
        newShutdownBlockingExecutor(1, 1);

        TryAwaitShutdownThread awaitThread = scheduleTryAwaitShutdown(0, startInterrupted);
        joinAll(awaitThread);

        awaitThread.assertSuccess();
        awaitThread.assertIsTerminatedWithInterruptFlag(startInterrupted);
        assertIsShutdown();
    }
}

