/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.concurrenttesting.TestRunnable;
import org.codehaus.prometheus.concurrenttesting.Delays;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;

/**
 * Unittests the {@link ThreadPoolBlockingExecutor#shutdownNow()} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor_ShutdownNowTest extends ThreadPoolBlockingExecutor_AbstractTest {

    public void testWhileUnstarted() {
        newUnstartedBlockingExecutor(1, 1);

        spawned_assertShutdownNow();

        assertIsShutdown();
        threadFactory.assertNoneCreated();
    }

    public void testWhileRunning_emptyPool() {
        newStartedBlockingExecutor(10, 0);

        spawned_assertShutdownNow();

        giveOthersAChance();
        assertIsShutdown();
        threadFactory.assertNoneCreated();
    }

    //there are workers, but there is nothing to do
    public void testWhileRunning_workersAreIdle() {
        int poolsize = 3;
        newStartedBlockingExecutor(10, poolsize);

        spawned_assertShutdownNow();

        giveOthersAChance();
        assertIsShutdown();
        threadFactory.assertCreatedCount(poolsize);
        threadFactory.assertAllAreNotAlive();
    }

    //there are workers and there is no unprocessed work
    public void testWhileRunning_activeWorkersAndNoUnprocessedWork() {
        int poolsize = 2;
        newStartedBlockingExecutor(0, poolsize);

        executeEonTask(poolsize);
        giveOthersAChance();

        spawned_assertShutdownNow();

        giveOthersAChance();
        assertIsShutdown();
        threadFactory.assertCreatedAndNotAliveCount(poolsize);
    }

    //there are active workers and there is also some unprocessed work
    public void testWhileRunning_activeWorkersUnprocessedWork() {
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
        threadFactory.assertCreatedAndNotAliveCount(poolsize);
        unprocessed1.assertBeginExecutionCount(0);
        unprocessed2.assertBeginExecutionCount(0);
        unprocessed3.assertBeginExecutionCount(0);

        runnable1.assertBeginExecutionCount(1);
        runnable2.assertBeginExecutionCount(1);
        
        //check that the running tasks were interrupted
    }

    public void testWhileShuttingdown() {
        newShuttingdownBlockingExecutor(Delays.EON_MS);
        int createdCount = threadFactory.getThreadCount();

        spawned_assertShutdownNow();

        giveOthersAChance();
        assertIsShutdown();
        threadFactory.assertCreatedAndNotAliveCount(createdCount);

        //todo: check that original task has been
    }

    public void testWhileForcedShuttingdown(){
        int poolsize = 1;
        newForcedShuttingdownBlockingExecutor(Delays.LONG_MS,poolsize);

        spawned_assertShutdownNow();
        giveOthersAChance();
        assertIsShuttingdown();
        threadFactory.assertCreatedAndAliveCount(poolsize);
    }

    public void testWhileShutdown() {
        newShutdownBlockingExecutor();
        int oldThreadCount = threadFactory.getThreadCount();

        spawned_assertShutdownNow();

        giveOthersAChance();
        assertIsShutdown();
        threadFactory.assertCreatedAndNotAliveCount(oldThreadCount);
    }
}
