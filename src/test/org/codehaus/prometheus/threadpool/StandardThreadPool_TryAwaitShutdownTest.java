package org.codehaus.prometheus.threadpool;

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
        newUnstartedThreadPool(10);
        assertShutdownNotifiesWaiters();
    }

    public void testWhileStarted() {
        newStartedThreadpool(10);
        assertShutdownNotifiesWaiters();
    }

    private void assertShutdownNotifiesWaiters() {
        int oldpoolsize = threadpool.getActualPoolSize();

        TryAwaitShutdownThread awaitThread1 = scheduleTryAwaitShutdown(DELAY_EON_MS);
        TryAwaitShutdownThread awaitThread2 = scheduleTryAwaitShutdown(DELAY_EON_MS);

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
        newShuttingdownThreadpool(10, 2 * DELAY_MEDIUM_MS);
        assertShutdownNotifiesWaiters();
    }

    public void testWhileShutdown() {
        newShutdownThreadpool();

        TryAwaitShutdownThread awaitThread = scheduleTryAwaitShutdown(0);
        joinAll(awaitThread);

        awaitThread.assertIsTerminatedNormally();
    }

    public void testTimedOut() {
        newStartedThreadpool(10);

        TryAwaitShutdownThread awaitThread1 = scheduleTryAwaitShutdown(DELAY_MEDIUM_MS);
        TryAwaitShutdownThread awaitThread2 = scheduleTryAwaitShutdown(DELAY_MEDIUM_MS);

        //check that the await wasn't successful immediately.
        giveOthersAChance(DELAY_TINY_MS);
        awaitThread1.assertIsStarted();
        awaitThread2.assertIsStarted();

        joinAll(awaitThread1, awaitThread2);
        awaitThread1.assertIsTimedOut();
        awaitThread2.assertIsTimedOut();
        assertIsStarted();
    }

    public void testInterruptedWhileWaiting() {
        newStartedThreadpool(10);

        TryAwaitShutdownThread awaitThread1 = scheduleTryAwaitShutdown(DELAY_EON_MS);
        TryAwaitShutdownThread awaitThread2 = scheduleTryAwaitShutdown(DELAY_EON_MS);

        //check that the await wasn't successful immediately.
        giveOthersAChance();
        awaitThread1.assertIsStarted();
        awaitThread2.assertIsStarted();

        awaitThread1.interrupt();

        //interrupt thread1 and see that thread1 is interrupted, and thread2 still is waiting
        joinAll(awaitThread1);
        awaitThread1.assertIsInterruptedByException();
        awaitThread2.assertIsStarted();
        assertIsStarted();
    }
}
