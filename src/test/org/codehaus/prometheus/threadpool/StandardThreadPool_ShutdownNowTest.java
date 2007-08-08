/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.testsupport.TestRunnable;
import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;

import java.util.List;

/**
 * Unittests the {@link StandardThreadPool#shutdownNow()} method.
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool_ShutdownNowTest extends StandardThreadPool_AbstractTest {

    public void testWhileUnstarted() {
        newUnstartedThreadPool(10);

        spawned_shutdownNow();

        assertIsShutdown();
        threadPoolThreadFactory.assertNoThreadsCreated();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileRunningAndEmptyPool() {
        newStartedThreadpool(0);

        spawned_shutdownNow();

        assertIsShutdown();
        threadPoolThreadFactory.assertNoThreadsCreated();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testIdleWorkersAreInterrupted() {
        int poolsize = 10;
        newStartedThreadpool(poolsize);

        spawned_shutdownNow();

        giveOthersAChance();
        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
        threadPoolThreadFactory.assertAllThreadsAreTerminated();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testNonIdleWorkersAreInterrupted() throws InterruptedException {
        int poolsize = 3;
        newStartedThreadpool(poolsize);
        ensureNoIdleWorkers();

        giveOthersAChance();
        spawned_shutdownNow();

        giveOthersAChance();
        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
        threadPoolThreadFactory.assertAllThreadsAreTerminated();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileShuttingDown_workersAreInterruptible() {
        int poolsize = 3;

        List<TestRunnable> list = newShuttingdownThreadpool(poolsize, DELAY_EON_MS);

        spawned_shutdownNow();

        giveOthersAChance();
        //if the running thread wasn't interrupted, the pool would not shut down.
        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
        threadPoolThreadFactory.assertAllThreadsAreTerminated();
        threadPoolExceptionHandler.assertNoErrors();
        //for(TestRunnable r: list)
        //    r.        
        //todo: checks need to be done if interrupt is caught on task
    }

    public void testWhileShuttingDown_workersAreNotInterruptible() {
        int poolsize = 3;
        newShuttingdownThreadpool(poolsize, DELAY_LONG_MS, false);

        spawned_shutdownNow();
        giveOthersAChance();
        assertIsForcedShuttingdown();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
        threadPoolThreadFactory.assertAllThreadsAreAlive();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileForcedShuttingdown() {
        int poolsize = 3;

        newForcedShuttingdownThreadpool(poolsize, DELAY_LONG_MS);
        spawned_shutdownNow();

        giveOthersAChance();
        assertIsForcedShuttingdown();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
        threadPoolThreadFactory.assertAllThreadsAreAlive();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileShutdown() {
        newShutdownThreadpool();
        int oldpoolsize = threadpool.getActualPoolSize();

        spawned_shutdownNow();

        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(oldpoolsize);
        threadPoolThreadFactory.assertAllThreadsAreTerminated();
        threadPoolExceptionHandler.assertNoErrors();
    }
}
