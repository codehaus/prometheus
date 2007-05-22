package org.codehaus.prometheus.threadpool;

/**
 * Unittests the {@link StandardThreadPool#shutdownNow()} method.
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool_ShutdownNowTest extends StandardThreadPool_AbstractTest {

    public void testWhileUnstarted() {
        newUnstartedThreadPool(10);

        shutdownNow();

        assertIsShutdown();
        threadPoolThreadFactory.assertNoThreadsCreated();
    }

    public void testWhileRunningAndEmptyPool() {
        newStartedThreadpool(0);

        shutdownNow();

        assertIsShutdown();
        threadPoolThreadFactory.assertNoThreadsCreated();
    }

    public void testIdleWorkersAreInterrupted() {
        int poolsize = 10;
        newStartedThreadpool(poolsize);

        shutdownNow();

        giveOthersAChance();
        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
    }

    public void testNonIdleWorkersAreInterrupted() throws InterruptedException {
        int poolsize = 3;
        newStartedThreadpool(poolsize);
        ensureNoIdleWorkers();

        giveOthersAChance();
        shutdownNow();

        giveOthersAChance(DELAY_MEDIUM_MS);
        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
    }


    public void testWhileShuttingDown() {
        int poolsize = 3;
        newShuttingdownThreadpool(poolsize, DELAY_EON_MS);

        shutdownNow();

        giveOthersAChance();
        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
    }

    public void testWhileShutdown() {
        newShutdownThreadpool();
        int oldpoolsize = threadpool.getActualPoolSize();

        shutdownNow();

        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(oldpoolsize);
    }

    private void shutdownNow() {
        ShutdownNowThread shutdownThread = scheduleShutdownNow();

        joinAll(shutdownThread);
        shutdownThread.assertIsTerminatedNormally();
    }
}
