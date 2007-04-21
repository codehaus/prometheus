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
 * Unittests the {@link ThreadPoolBlockingExecutor#tryAwaitShutdown(long, TimeUnit)} method.
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
        newStartedBlockingExecutor(1,1,new SleepingRunnable(500));

        TryAwaitShutdownThread awaitThread = scheduleTryAwaitShutdown(100);
        joinAll(awaitThread);

        awaitThread.assertIsTimedOut();
        assertIsRunning();
    }

    public void testSomeWaitingNeeded(){
        newStartedBlockingExecutor(1,1,new SleepingRunnable(DELAY_SMALL_MS));
        executor.shutdownNow();
        fail();
    }

    public void testNotStarted() {
        fail();
    }

    public void testStarted() {
        fail();
    }

    public void testShuttingDown() {
        fail();
    }

    public void testShutdown() throws InterruptedException {
        newShutdownBlockingExecutor(1,1);

        TryAwaitShutdownThread awaitThread = scheduleTryAwaitShutdown(0);
        joinAll(awaitThread);

        awaitThread.assertSuccess();
        assertIsShutdown();
    }

    public void testInterruptedWhileWaiting() {
        fail();
    }

    public void testSpuriousWakeup() {
        fail();
    }
}

