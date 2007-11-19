/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.concurrenttesting.SleepingRunnable;
import org.codehaus.prometheus.concurrenttesting.TestRunnable;
import org.codehaus.prometheus.concurrenttesting.Delays;
import static org.codehaus.prometheus.concurrenttesting.TestSupport.newEonSleepingRunnable;
import static org.codehaus.prometheus.concurrenttesting.TestSupport.newSleepingRunnable;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.joinAll;

import java.util.concurrent.RejectedExecutionException;

/**
 * Unittests the {@link ThreadPoolBlockingExecutor#execute(Runnable)} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor_ExecuteTest extends ThreadPoolBlockingExecutor_AbstractTest {

    public void testArguments() throws InterruptedException {
        newStartedBlockingExecutor(1, 1);

        try {
            executor.execute(null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testWhileUnstarted() {
        newUnstartedBlockingExecutor(1, 1);
        assertExecuteIsRejected();
    }

    public void testInterruptedWhileWaiting() {
        int poolsize = 1;
        newStartedBlockingExecutor(0, poolsize);

        //place first task
        spawned_placeSleepingTask(Delays.EON_MS);

        //place the second task. The placement of this task block because there is no space
        SleepingRunnable secondTask = newEonSleepingRunnable();
        ExecuteThread executeThread = scheduleExecute(secondTask, START_UNINTERRUPTED);

        //make sure that executeThread is blocking
        giveOthersAChance();
        executeThread.assertIsStarted();

        //interrupt the executingThread
        executeThread.interrupt();
        joinAll(executeThread);

        executeThread.assertIsTerminatedByInterruptedException();
        assertActualPoolSize(poolsize);
        assertIsRunning();
        secondTask.assertIsUnstarted();
    }

    public void testSuccess_noIdleWorkersButQueueHasSpace() {
        newStartedBlockingExecutor(10, 0);

        SleepingRunnable task = spawned_placeSleepingTask(Delays.EON_MS);

        giveOthersAChance();
        assertActualPoolSize(0);
        assertWorkQueueContains(task);
        assertIsRunning();
        task.assertIsUnstarted();
    }

    public void testSuccess_noIdleWorkersAndQueueHasSpace() {
        newStartedBlockingExecutor(10, 1);

        SleepingRunnable task = spawned_placeSleepingTask(Delays.EON_MS);

        giveOthersAChance();
        assertActualPoolSize(1);
        assertWorkQueueContains();
        assertIsRunning();
        task.assertIsStarted();
    }

    //there are idle workers but there is no queue capacity (synchronousqueue is used)
    public void testSucccess_idleWorkersAndQueueHasNoSpace() throws InterruptedException {
        int poolsize = 10;
        newStartedBlockingExecutor(0, poolsize);

        SleepingRunnable task = spawned_placeSleepingTask(Delays.EON_MS);

        giveOthersAChance();
        assertActualPoolSize(poolsize);
        assertWorkQueueIsEmpty();
        assertIsRunning();
        task.assertIsStarted();
    }

    public void testShuttingdown() {
        newShuttingdownBlockingExecutor(Delays.EON_MS);
        assertExecuteIsRejected();
    }

    public void testWhileForcedShuttingdown() {
        int poolsize = 3;
        newForcedShuttingdownBlockingExecutor(Delays.LONG_MS,poolsize);
        assertExecuteIsRejected();
    }

    public void testWhileShutdown() {
        newShutdownBlockingExecutor(1, 1);
        assertExecuteIsRejected();
    }

    private void assertExecuteIsRejected() {
        BlockingExecutorServiceState oldState = executor.getState();
        TestRunnable task = new TestRunnable();

        ExecuteThread t = scheduleExecute(task, false);
        joinAll(t);
        t.assertIsTerminatedWithThrowing(RejectedExecutionException.class);

        giveOthersAChance();
        assertHasState(oldState);
        task.assertNotExecuted();
    }

    public void testShutdownWhileWaitingForPlacement() {
        //create an executor with a single thread, and a synchronous queue
        newStartedBlockingExecutor(0, 1);

        //make sure that the executor is running some task
        TestRunnable initialTask = newSleepingRunnable(Delays.LONG_MS);
        spawned_execute(initialTask);

        //now place the second task. This call blocks untill space is available.
        TestRunnable task = newSleepingRunnable(Delays.LONG_MS);
        ExecuteThread executeThread = scheduleExecute(task, START_UNINTERRUPTED);
        giveOthersAChance();
        executeThread.assertIsStarted();

        //in the meanwhile we are going to shutdown the blockingexecutor
        spawned_shutdownPolitly();
        assertIsShuttingdown();

        //now check that the executeThread fails with a RejectedExecutionException because
        //the blockingexecutor is shutting down.
        joinAll(executeThread);
        executeThread.assertIsTerminatedNormally();
    }
}
