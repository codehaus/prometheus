package org.codehaus.prometheus.threadpool;

/**
 * Unittests the {@link StandardThreadPool#shutdown()} method.
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool_ShutdownTest extends StandardThreadPool_AbstractTest {

    public void testWhileUnstarted() {
        newUnstartedThreadPool(10);

        ShutdownThread shutdownThread = scheduleShutdown();
        joinAll(shutdownThread);

        shutdownThread.assertIsTerminatedWithoutThrowing();
        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(0);
    }

    public void testIdleWorkersAreInterrupted() {
        int poolsize = 10;
        newStartedThreadpool(poolsize);

        ShutdownThread shutdownThread = scheduleShutdown();
        joinAll(shutdownThread);

        sleepMs(DELAY_SMALL_MS);
        assertIsShutdown();
        assertEquals(0, threadpool.getActualPoolSize());
        threadPoolThreadFactory.assertCreatedCount(poolsize);
    }

    public void testNonIdleWorkerIsNotInterrupted() {
        int poolsize = 10;
        newStartedThreadpool(poolsize);

        letWorkersWork(poolsize,DELAY_EON_MS);
        //give workers time to start executing the task.
        sleepMs(DELAY_SMALL_MS);
        //make sure that all workers are executing a job.
        assertTrue(taskQueue.isEmpty());

        ShutdownThread shutdownThread = scheduleShutdown();
        joinAll(shutdownThread);

        sleepMs(DELAY_SMALL_MS);
        assertIsShuttingdown();

        assertEquals(poolsize, threadpool.getActualPoolSize());
        threadPoolThreadFactory.assertCreatedCount(poolsize);
    }

    public void testWhileRunningButEmptyPool() {
        newStartedThreadpool(0);

        ShutdownThread shutdownThread = scheduleShutdown();
        joinAll(shutdownThread);
        shutdownThread.assertIsTerminatedWithoutThrowing();

        sleepMs(DELAY_SMALL_MS);

        assertIsShutdown();
        assertEquals(0, threadpool.getActualPoolSize());
        threadPoolThreadFactory.assertCreatedCount(0);
    }

    public void testWhileShuttingDown_allWorkersAreInterrupted() {
        int poolsize = 10;
        newStartedThreadpool(poolsize);

        letWorkersWork(poolsize,DELAY_EON_MS);
        //give workers time to start executing the task.
        sleepMs(DELAY_SMALL_MS);
        //make sure that all workers are executing a job.
        assertTrue(taskQueue.isEmpty());

        //shut down the threadpool
        ShutdownThread shutdownThread = scheduleShutdown();
        joinAll(shutdownThread);
        shutdownThread.assertIsTerminatedWithoutThrowing();
        assertIsShuttingdown();
        sleepMs(DELAY_SMALL_MS);

        //give the shutdown now command
        ShutdownNowThread shutdownNowThread = scheduleShutdownNow();
        joinAll(shutdownNowThread);
        shutdownThread.assertIsTerminatedWithoutThrowing();

        //check that all workers have terminated.
        sleepMs(DELAY_SMALL_MS);
        assertIsShutdown();
        assertEquals(0, threadpool.getActualPoolSize());
        threadPoolThreadFactory.assertCreatedCount(poolsize);
    }

    public void testWhileShutdown() {
        newShutdownThreadpool();

        ShutdownThread shutdownThread = scheduleShutdown();
        joinAll(shutdownThread);

        shutdownThread.assertIsTerminatedWithoutThrowing();
        assertIsShutdown();
    }
}
