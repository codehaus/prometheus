package org.codehaus.prometheus.threadpool;

import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;

/**
 * Unittests the {@link StandardThreadPool#shutdown()} method.
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool_ShutdownTest extends StandardThreadPool_AbstractTest {

    public void testWhileUnstarted() {
        newUnstartedThreadPool(10);

        spawned_assertShutdown();

        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(0);
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileRunningButEmptyPool() {
        newStartedThreadpool(0);

        spawned_assertShutdown();
        giveOthersAChance();
        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(0);
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testIdleWorkersAreInterrupted() {
        int poolsize = 10;
        newStartedThreadpool(poolsize);

        spawned_assertShutdown();

        giveOthersAChance();

        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
        threadPoolThreadFactory.assertThreadsHaveTerminated();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testNonIdleWorkerAreNotInterrupted() {
        int poolsize = 10;
        newStartedThreadpool(poolsize);

        ensureNoIdleWorkers(DELAY_EON_MS);
        //give workers time to spawned_start executing the task.
        giveOthersAChance();
        //make sure that all workers are executing a job.
        assertTrue(taskQueue.isEmpty());

        spawned_assertShutdown();

        giveOthersAChance();
        assertIsShuttingdown();
        assertActualPoolsize(poolsize);
        threadPoolThreadFactory.assertCreatedCount(poolsize);
        threadPoolThreadFactory.assertAllThreadsAlive();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileShuttingDown_nonIdleWorkersAreNotInterrupted() {
        int poolsize = 10;
        newStartedThreadpool(poolsize);

        ensureNoIdleWorkers(DELAY_EON_MS);

        spawned_assertShutdown();

        giveOthersAChance();

        assertIsShuttingdown();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
        threadPoolThreadFactory.assertAllThreadsAlive();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileForcedShuttingdown(){
        int poolsize = 3;
        newForcedShuttingdownThreadpool(poolsize,DELAY_LONG_MS);

        spawned_assertShutdown();
        
        giveOthersAChance();

        assertIsForcedShuttingdown();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
        threadPoolThreadFactory.assertAllThreadsAlive();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileShutdown() {
        newShutdownThreadpool();

        spawned_assertShutdown();

        assertIsShutdown();
    }
}
