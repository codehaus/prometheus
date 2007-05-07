/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

/**
 * Unittests the {@link ThreadPoolBlockingExecutor#shutdown()} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor_ShutdownTest extends ThreadPoolBlockingExecutor_AbstractTest {

    public void testNotStarted() {
        newUnstartedBlockingExecutor(1, 10);

        shutdown();

        assertIsShutdown();
        threadFactory.assertNoThreadsCreated();
    }

    public void testRunning_noWorkersNoUnprocessedWork() {
        newStartedBlockingExecutor(1, 0);

        shutdown();
        assertIsShutdown();
        threadFactory.assertNoThreadsCreated();
    }

    public void testRunning_workersAreIdle() {
        int poolsize = 10;
        newStartedBlockingExecutor(0, poolsize);

        shutdown();

        giveOthersAChance();
        assertIsShutdown();
        threadFactory.assertCreatedCount(poolsize);
    }

    public void testRunning_workersAreNotIdle() {
        int poolsize = 10;
        newStartedBlockingExecutor(0, poolsize);

        //let all workers work for ever
        executeEonTask(poolsize);

        giveOthersAChance();

        shutdown();

        //check all invariants
        assertIsShuttingDown();
        threadFactory.assertCreatedCount(poolsize);
        threadFactory.assertAllThreadsAlive();
    }

    public void testShutdownWhileShuttingDown() {
        newShuttingDownBlockingExecutor(DELAY_EON_MS);

        shutdown();

        assertIsShuttingDown();
        threadFactory.assertCreatedCount(1);
        threadFactory.assertAllThreadsAlive();
    }

    public void testShutdownWhileShutdown() {
        newShutdownBlockingExecutor(1, 5);
        int oldPoolsize = threadFactory.getThreadCount();

        shutdown();

        assertIsShutdown();
        threadFactory.assertCreatedCount(oldPoolsize);
    }

    private void shutdown() {
        ShutdownThread shutdownThread = scheduleShutdown();
        joinAll(shutdownThread);
        shutdownThread.assertIsTerminatedWithoutThrowing();
    }
}


