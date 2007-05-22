package org.codehaus.prometheus.threadpool;

/**
 * Unittests the {@link StandardThreadPool#shutdown()} method.
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool_ShutdownTest extends StandardThreadPool_AbstractTest {

    public void testWhileUnstarted() {
        newUnstartedThreadPool(10);

        shutdown();

        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(0);
    }

    public void testWhileRunningButEmptyPool() {
        newStartedThreadpool(0);

        shutdown();

        sleepMs(DELAY_SMALL_MS);

        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(0);
    }

    public void testIdleWorkersAreInterrupted() {
        int poolsize = 10;
        newStartedThreadpool(poolsize);

        shutdown();

        sleepMs(DELAY_SMALL_MS);

        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
    }

    public void testNonIdleWorkerAreNotInterrupted() {
        int poolsize = 10;
        newStartedThreadpool(poolsize);

        ensureNoIdleWorkers(DELAY_EON_MS);
        //give workers time to start executing the task.
        sleepMs(DELAY_SMALL_MS);
        //make sure that all workers are executing a job.
        assertTrue(taskQueue.isEmpty());

        shutdown();

        sleepMs(DELAY_SMALL_MS);

        assertIsShuttingdown();
        assertActualPoolsize(poolsize);
        threadPoolThreadFactory.assertCreatedCount(poolsize);
    }

    public void testWhileShuttingDown_workersAreNotInterrupted() {
        int poolsize = 10;
        newStartedThreadpool(poolsize);

        ensureNoIdleWorkers(DELAY_EON_MS);
        //give workers time to start executing the task.
        sleepMs(DELAY_SMALL_MS);
        //make sure that all workers are executing a job.
        assertTrue(taskQueue.isEmpty());

        shutdown();

        sleepMs(DELAY_SMALL_MS);

        assertIsShuttingdown();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
    }

    public void testWhileShutdown() {
        newShutdownThreadpool();

        shutdown();

        assertIsShutdown();
    }


    private ShutdownThread shutdown() {
        ShutdownThread shutdownThread = scheduleShutdown();
        joinAll(shutdownThread);
        shutdownThread.assertIsTerminatedNormally();
        return shutdownThread;
    }
}
