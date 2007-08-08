/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
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

        spawned_shutdown();

        assertIsShutdown();
        threadPoolThreadFactory.assertNoThreadsCreated();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileRunningButEmptyPool() {
        newStartedThreadpool(0);

        spawned_shutdown();
        giveOthersAChance();
        assertIsShutdown();
        threadPoolThreadFactory.assertNoThreadsCreated();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testIdleWorkersAreInterrupted() {
        int poolsize = 10;
        newStartedThreadpool(poolsize);

        spawned_shutdown();

        giveOthersAChance();

        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
        threadPoolThreadFactory.assertAllThreadsAreTerminated();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testNonIdleWorkerAreNotInterrupted() {
        int poolsize = 10;
        newStartedThreadpool(poolsize);

        ensureNoIdleWorkers(DELAY_EON_MS);
        //give workers time to spawned_start executing the task.
        giveOthersAChance();
        //make sure that all workers are executing a job.
        assertTrue(workQueue.isEmpty());

        spawned_shutdown();

        giveOthersAChance();
        assertIsShuttingdown();
        assertActualPoolsize(poolsize);
        threadPoolThreadFactory.assertCreatedCount(poolsize);
        threadPoolThreadFactory.assertAllThreadsAreAlive();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileShuttingDown_nonIdleWorkersAreNotInterrupted() {
        int poolsize = 10;
        newStartedThreadpool(poolsize);

        ensureNoIdleWorkers(DELAY_EON_MS);

        spawned_shutdown();

        giveOthersAChance();

        assertIsShuttingdown();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
        threadPoolThreadFactory.assertAllThreadsAreAlive();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileForcedShuttingdown(){
        int poolsize = 3;
        newForcedShuttingdownThreadpool(poolsize,DELAY_LONG_MS);

        spawned_shutdown();
        
        giveOthersAChance();

        assertIsForcedShuttingdown();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
        threadPoolThreadFactory.assertAllThreadsAreAlive();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileShutdown() {
        newShutdownThreadpool();

        int oldCreatedCount = threadPoolThreadFactory.getThreadCount();

        spawned_shutdown();

        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(oldCreatedCount);
        threadPoolThreadFactory.assertAllThreadsAreTerminated();
        threadPoolExceptionHandler.assertNoErrors();
    }
}
