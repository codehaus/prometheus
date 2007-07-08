/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;

/**
 * Unittests the {@link ThreadPoolBlockingExecutor#shutdown()} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor_ShutdownTest extends ThreadPoolBlockingExecutor_AbstractTest {

    public void testWhileUnstarted() {
        newUnstartedBlockingExecutor(1, 10);

        spawned_assertShutdown();

        assertIsShutdown();
        threadFactory.assertNoThreadsCreated();
    }

    public void testWhileRunning_noWorkersNoUnprocessedWork() {
        newStartedBlockingExecutor(1, 0);

        spawned_assertShutdown();

        assertIsShutdown();
        threadFactory.assertNoThreadsCreated();
    }

    public void testWhileRunning_workersAreIdle() {
        int poolsize = 10;
        newStartedBlockingExecutor(0, poolsize);

        spawned_assertShutdown();

        giveOthersAChance();
        assertIsShutdown();
        threadFactory.assertCreatedAndTerminatedCount(poolsize);
    }

    public void testWhileRunning_workersAreNotIdle() {
        int poolsize = 10;
        newStartedBlockingExecutor(0, poolsize);

        //let all workers work for ever
        executeEonTask(poolsize);

        giveOthersAChance();

        spawned_assertShutdown();

        //check all invariants
        assertIsShuttingdown();
        threadFactory.assertCreatedAndAliveCount(poolsize);
    }

    public void testWhileShuttingdown() {
        newShuttingdownBlockingExecutor(DELAY_EON_MS);
        assertShutdownIsIgnored();
    }

    public void testWhileForcedShuttingdown() {
        int poolsize = 4;
        newForcedShuttingdownBlockingExecutor(DELAY_LONG_MS, poolsize);
        assertShutdownIsIgnored();
    }

    public void testWhileShutdown() {
        newShutdownBlockingExecutor(1, 5);
        assertShutdownIsIgnored();
    }

    private void assertShutdownIsIgnored() {
        int oldThreadCount = threadFactory.getThreadCount();
        BlockingExecutorServiceState oldState = executor.getState();

        spawned_assertShutdown();

        assertHasState(oldState);
        threadFactory.assertCreatedAndAliveCount(oldThreadCount);
    }
}


