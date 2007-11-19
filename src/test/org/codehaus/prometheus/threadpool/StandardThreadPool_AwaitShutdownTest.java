/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.joinAll;
import org.codehaus.prometheus.concurrenttesting.Delays;

/**
 * Unittests the {@link StandardThreadPool#awaitShutdown()} method.
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool_AwaitShutdownTest extends StandardThreadPool_AbstractTest {

    public void testWhileUnstarted() {
        newUnstartedThreadPool(10);

        assertShutdownTerminatesWaiters(0);
    }

    public void testWhileStarted() {
        int poolsize = 2;
        newStartedThreadpool(poolsize);

        assertShutdownTerminatesWaiters(poolsize);
    }

    public void assertShutdownTerminatesWaiters(int expectedThreadCreationCount) {
        AwaitShutdownThread awaitThread1 = scheduleAwaitShutdown();
        AwaitShutdownThread awaitThread2 = scheduleAwaitShutdown();

        giveOthersAChance();
        awaitThread1.assertIsStarted();
        awaitThread2.assertIsStarted();

        spawned_shutdown();

        //check that the awaiting threads spawned_shutdown
        joinAll(awaitThread1, awaitThread2);
        awaitThread1.assertIsTerminatedNormally();
        awaitThread2.assertIsTerminatedNormally();
        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(expectedThreadCreationCount);
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileShuttingdown() {
        int poolsize = 3;
        newShuttingdownThreadpool(poolsize, Delays.LONG_MS);
        assertShutdownTerminatesWaiters(poolsize);
    }

    public void testWhileForcedShuttingdown() {
        int poolsize = 3;
        newForcedShuttingdownThreadpool(poolsize, Delays.LONG_MS);
        assertShutdownTerminatesWaiters(poolsize);
    }

    public void testInterruptedWhileWaiting() {
        newStartedThreadpool(10);

        AwaitShutdownThread awaitThread1 = scheduleAwaitShutdown();
        AwaitShutdownThread awaitThread2 = scheduleAwaitShutdown();

        //check that the await wasn't successful immediately.
        giveOthersAChance();
        awaitThread1.assertIsStarted();
        awaitThread2.assertIsStarted();

        awaitThread1.interrupt();

        //interrupt thread1 and see that thread1 is interrupted, and thread2 still is waiting
        joinAll(awaitThread1);
        awaitThread1.assertIsTerminatedByInterruptedException();
        awaitThread2.assertIsStarted();
        assertIsRunning();        
    }

    public void testWhileShutdown() {
        newShutdownThreadpool();

        AwaitShutdownThread awaitThread1 = scheduleAwaitShutdown();
        AwaitShutdownThread awaitThread2 = scheduleAwaitShutdown();
        joinAll(awaitThread1, awaitThread2);

        awaitThread1.assertIsTerminatedNormally();
        awaitThread2.assertIsTerminatedNormally();
        assertIsShutdown();
    }
}
