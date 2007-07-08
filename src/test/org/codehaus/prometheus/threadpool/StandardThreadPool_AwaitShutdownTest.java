package org.codehaus.prometheus.threadpool;

import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;

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
        int poolsize = 10;
        newStartedThreadpool(poolsize);

        assertShutdownTerminatesWaiters(poolsize);
    }

    public void assertShutdownTerminatesWaiters(int expectedThreadCreationCount) {
        AwaitShutdownThread awaitThread1 = scheduleAwaitShutdown();
        AwaitShutdownThread awaitThread2 = scheduleAwaitShutdown();

        giveOthersAChance();
        awaitThread1.assertIsStarted();
        awaitThread2.assertIsStarted();

        spawned_assertShutdown();

        //check that the awaiting threads spawned_assertShutdown
        joinAll(awaitThread1, awaitThread2);
        awaitThread1.assertIsTerminatedNormally();
        awaitThread2.assertIsTerminatedNormally();
        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(expectedThreadCreationCount);
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileShuttingdown() {
        int poolsize = 3;
        newShuttingdownThreadpool(poolsize, DELAY_LONG_MS);
        assertShutdownTerminatesWaiters(poolsize);
    }

    public void testWhileForcedShuttingdown() {
        int poolsize = 3;
        newForcedShuttingdownThreadpool(poolsize, DELAY_LONG_MS);
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
        awaitThread1.assertIsInterruptedByException();
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
