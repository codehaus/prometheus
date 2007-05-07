/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.testsupport.SleepingRunnable;

/**
 * Unittests the {@link ThreadPoolBlockingExecutor#shutdownNow()} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor_ShutdownNowTest extends ThreadPoolBlockingExecutor_AbstractTest {


    public void testUnstarted() {
        newUnstartedBlockingExecutor(1, 1);

        shutdownNow();

        assertIsShutdown();
        threadFactory.assertNoThreadsCreated();
        assertWorkQueueIsEmpty();
    }

    public void testRunning_emptyPool() {
        newStartedBlockingExecutor(10, 0);

        shutdownNow();

        giveOthersAChance();
        assertIsShutdown();
        threadFactory.assertNoThreadsCreated();
        assertWorkQueueIsEmpty();
    }

    //there are workers, but there is nothing to do
    public void testRunning_completelyIdle() {
        int poolsize = 3;
        newStartedBlockingExecutor(10, poolsize);

        shutdownNow();

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

        shutdownNow();

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
        executeEonTask();
        executeEonTask();

        //tasks that are not processed
        SleepingRunnable unprocessed1 = executeEonTask();
        SleepingRunnable unprocessed2 = executeEonTask();
        SleepingRunnable unprocessed3 = executeEonTask();

        shutdownNow(unprocessed1, unprocessed2, unprocessed3);

        giveOthersAChance();
        assertIsShutdown();
        threadFactory.assertCreatedCount(2);
        //check that the unprocessed tasks, have not been run
        //check that the running tasks were interrupted
    }

    public void testShuttingDown() {
        newShuttingDownBlockingExecutor(DELAY_EON_MS);

        shutdownNow();

        giveOthersAChance();
        assertIsShutdown();
        threadFactory.assertCreatedCount(1);
    }

    public void testShutdown() {
        newShutdownBlockingExecutor(1, 10);
        int oldPoolsize = executor.getThreadPool().getActualPoolSize();

        shutdownNow();

        giveOthersAChance();
        assertIsShutdown();
        threadFactory.assertCreatedCount(oldPoolsize);
    }

    private ShutdownNowThread shutdownNow(Runnable... expectedUnprocessed) {
        ShutdownNowThread shutdownThread = scheduleShutdownNow();
        joinAll(shutdownThread);
        shutdownThread.assertSuccess(expectedUnprocessed);
        return shutdownThread;
    }
}
