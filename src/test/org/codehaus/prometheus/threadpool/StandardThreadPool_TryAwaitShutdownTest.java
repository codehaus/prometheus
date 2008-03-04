/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.joinAll;
import org.codehaus.prometheus.concurrenttesting.Delays;

import java.util.concurrent.TimeoutException;

/**
 * Unittests the {@link StandardThreadPool#tryAwaitShutdown(long,java.util.concurrent.TimeUnit)} method.
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool_TryAwaitShutdownTest extends StandardThreadPool_AbstractTest {

    public void testArgument() throws TimeoutException, InterruptedException {
        newShutdownThreadpool();

        try {
            threadpool.tryAwaitShutdown(1, null);
            fail();
        } catch (NullPointerException ex) {
        }
    }

    public void testWhileUnstarted() {
        newUnstartedThreadPool(3);
        assertShutdownNotifiesWaiters();
    }

    public void testWhileStarted() {
        newStartedThreadpool(3);
        assertShutdownNotifiesWaiters();
    }

    private void assertShutdownNotifiesWaiters() {
        int oldpoolsize = threadpool.getActualPoolSize();

        TryAwaitShutdownThread awaitThread1 = scheduleTryAwaitShutdown(Delays.EON_MS);
        TryAwaitShutdownThread awaitThread2 = scheduleTryAwaitShutdown(Delays.EON_MS);

        giveOthersAChance();
        awaitThread1.assertIsStarted();
        awaitThread2.assertIsStarted();

        //now shutdown and see that the awaitThread has terminated.
        ShutdownThread shutdownThread = scheduleShutdown();

        joinAll(shutdownThread, awaitThread1, awaitThread2);
        shutdownThread.assertIsTerminatedNormally();
        awaitThread1.assertIsTerminatedNormally();
        awaitThread2.assertIsTerminatedNormally();
        threadPoolThreadFactory.assertCreatedCount(oldpoolsize);
        assertIsShutdown();
    }

    public void testWhileShuttingDown() {
        newShuttingdownThreadpool(3, 2 * Delays.MEDIUM_MS);
        assertShutdownNotifiesWaiters();
    }

    public void testWhileForcedShuttingdown() {
        newForcedShuttingdownThreadpool(3, Delays.MEDIUM_MS);
        assertShutdownNotifiesWaiters();
    }

    public void testWhileShutdown() {
        newShutdownThreadpool();

        TryAwaitShutdownThread awaitThread = scheduleTryAwaitShutdown(0);
        joinAll(awaitThread);

        awaitThread.assertIsTerminatedNormally();
    }

    public void testTimedOut() {
        newStartedThreadpool(3);

        TryAwaitShutdownThread awaitThread1 = scheduleTryAwaitShutdown(Delays.MEDIUM_MS);
        TryAwaitShutdownThread awaitThread2 = scheduleTryAwaitShutdown(Delays.MEDIUM_MS);

        //check that the await wasn't successful immediately.
        giveOthersAChance();
        awaitThread1.assertIsStarted();
        awaitThread2.assertIsStarted();

        //check that both awaits are timed out.
        joinAll(awaitThread1, awaitThread2);
        awaitThread1.assertIsTimedOut();
        awaitThread2.assertIsTimedOut();
        assertIsRunning();
    }

    public void testInterruptedWhileWaiting() {
        newStartedThreadpool(3);

        TryAwaitShutdownThread awaitThread1 = scheduleTryAwaitShutdown(Delays.EON_MS);
        TryAwaitShutdownThread awaitThread2 = scheduleTryAwaitShutdown(Delays.EON_MS);

        //check that the await wasn't successful immediately.
        giveOthersAChance();
        awaitThread1.assertIsStarted();
        awaitThread2.assertIsStarted();

        //interrupt thread1 and see that thread1 is interrupted, and thread2 still is waiting
        awaitThread1.interrupt();
        joinAll(awaitThread1);
        awaitThread1.assertIsTerminatedByInterruptedException();
        awaitThread2.assertIsStarted();
        assertIsRunning();
    }
}
