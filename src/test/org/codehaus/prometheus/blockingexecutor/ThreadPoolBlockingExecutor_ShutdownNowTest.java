/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.testsupport.TestRunnable;

/**
 * Unittests the {@link ThreadPoolBlockingExecutor#shutdownNow()} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor_ShutdownNowTest extends ThreadPoolBlockingExecutor_AbstractTest {

    public void testUnstarted() {
        newUnstartedBlockingExecutor(1, 1);

        spawned_assertShutdownNow();

        assertIsShutdown();
        threadFactory.assertNoThreadsCreated();
        assertWorkQueueIsEmpty();
    }

    public void testRunning_emptyPool() {
        newStartedBlockingExecutor(10, 0);

        spawned_assertShutdownNow();

        giveOthersAChance();
        assertIsShutdown();
        threadFactory.assertNoThreadsCreated();
        assertWorkQueueIsEmpty();
    }

    //there are workers, but there is nothing to do
    public void testRunning_completelyIdle() {
        int poolsize = 3;
        newStartedBlockingExecutor(10, poolsize);

        spawned_assertShutdownNow();

        giveOthersAChance();
        assertIsShutdown();
        threadFactory.assertCreatedCount(poolsize);
        threadFactory.assertThreadsHaveTerminated();
        assertWorkQueueIsEmpty();
    }

    //there are workers and there is no unprocessed work
    public void testRunning_activeWorkersNoUnprocessedWork() {
        int poolsize = 2;
        newStartedBlockingExecutor(0, poolsize);

        executeEonTask(poolsize);
        giveOthersAChance();

        spawned_assertShutdownNow();

        giveOthersAChance();
        assertIsShutdown();
        threadFactory.assertCreatedCount(poolsize);
        //check that eon tasks were started        
    }

    //there are active workers and there is also some unprocessed work
    public void testRunning_activeWorkersUnprocessedWork() {
        int poolsize = 2;
        newStartedBlockingExecutor(10, poolsize);

        //tasks that are going to be processed
        TestRunnable runnable1 = executeEonTask();
        TestRunnable runnable2 = executeEonTask();

        //tasks that are not processed
        TestRunnable unprocessed1 = executeEonTask();
        TestRunnable unprocessed2 = executeEonTask();
        TestRunnable unprocessed3 = executeEonTask();

        spawned_assertShutdownNow(unprocessed1, unprocessed2, unprocessed3);

        giveOthersAChance();
        assertIsShutdown();
        threadFactory.assertCreatedCount(poolsize);
        unprocessed1.assertBeginExecutionCount(0);
        unprocessed2.assertBeginExecutionCount(0);
        unprocessed3.assertBeginExecutionCount(0);

        runnable1.assertBeginExecutionCount(1);
        runnable2.assertBeginExecutionCount(1);
        
        //check that the running tasks were interrupted
    }

    public void testShuttingDown() {
        newShuttingDownBlockingExecutor(DELAY_EON_MS);

        spawned_assertShutdownNow();

        giveOthersAChance();
        assertIsShutdown();
        threadFactory.assertCreatedCount(1);

        //todo: check that original task has been
    }

    public void testShutdown() {
        newShutdownBlockingExecutor();
        int oldThreadCount = threadFactory.getThreadCount();

        spawned_assertShutdownNow();

        giveOthersAChance();
        assertIsShutdown();
        threadFactory.assertCreatedCount(oldThreadCount);
    }
}
