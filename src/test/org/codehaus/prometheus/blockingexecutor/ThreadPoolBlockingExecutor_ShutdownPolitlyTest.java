/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;
import org.codehaus.prometheus.concurrenttesting.Delays;
import org.codehaus.prometheus.concurrenttesting.TestRunnable;
import static org.codehaus.prometheus.concurrenttesting.TestSupport.newDummyRunnable;

/**
 * Unittests the {@link ThreadPoolBlockingExecutor#shutdownPolitly()} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor_ShutdownPolitlyTest extends ThreadPoolBlockingExecutor_AbstractTest {

    public void testWhileUnstarted() {
        newUnstartedBlockingExecutor(1, 10);

        spawned_shutdownPolitly();

        assertIsShutdown();
        threadFactory.assertNoneCreated();
    }

    public void testWhileRunning_noWorkersNoUnprocessedWork() {
        newStartedBlockingExecutor(1, 0);

        spawned_shutdownPolitly();

        assertIsShutdown();
        threadFactory.assertNoneCreated();
    }

    public void testWhileRunning_noWorkersLeadToUnprocessedWork() {
        TestRunnable task1 = newDummyRunnable();
        TestRunnable task2 = newDummyRunnable();
        TestRunnable task3 = newDummyRunnable();

        newStartedBlockingExecutor(10, 0);
        spawned_execute(task1,task2,task3);

        spawned_shutdownPolitly(task1,task2,task3);

        task1.assertNotExecuted();
        task2.assertNotExecuted();
        task3.assertNotExecuted();
        assertIsShutdown();
        threadFactory.assertNoneCreated();
    }

    public void testWhileRunning_workersAreIdle() {
        int poolsize = 10;
        newStartedBlockingExecutor(0, poolsize);

        spawned_shutdownPolitly();

        giveOthersAChance();
        assertIsShutdown();
        threadFactory.assertCreatedAndNotAliveCount(poolsize);
    }

    public void testWhileRunning_workersAreNotIdle() {
        int poolsize = 10;
        newStartedBlockingExecutor(0, poolsize);

        //let all workers work for ever
        executeEonTask(poolsize);

        giveOthersAChance();

        spawned_shutdownPolitly();

        //check all invariants
        assertIsShuttingdown();
        threadFactory.assertCreatedAndAliveCount(poolsize);
    }

    public void testWhileShuttingdown() {
        newShuttingdownBlockingExecutor(Delays.EON_MS);
        assertShutdownIsIgnored();
    }

    public void testWhileForcedShuttingdown() {
        int poolsize = 4;
        newForcedShuttingdownBlockingExecutor(Delays.LONG_MS, poolsize);
        assertShutdownIsIgnored();
    }

    public void testWhileShutdown() {
        newShutdownBlockingExecutor(1, 5);
        assertShutdownIsIgnored();
    }

    private void assertShutdownIsIgnored() {
        int oldThreadCount = threadFactory.getThreadCount();
        BlockingExecutorServiceState oldState = executor.getState();

        spawned_shutdownPolitly();

        assertHasState(oldState);
        threadFactory.assertCreatedAndAliveCount(oldThreadCount);
    }
}


