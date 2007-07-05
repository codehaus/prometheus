/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.testsupport.CountingRunnable;
import org.codehaus.prometheus.testsupport.SleepingRunnable;
import org.codehaus.prometheus.testsupport.TestRunnable;

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

    public void testInterruptedWhileWaiting() {
        int poolsize = 1;
        newStartedBlockingExecutor(0, poolsize);

        //place first task
        spawned_placeTask(DELAY_EON_MS);

        //place the second task. The placement of this task block because there is no space
        SleepingRunnable secondTask = new SleepingRunnable(DELAY_EON_MS);
        ExecuteThread executeThread = scheduleExecute(secondTask, START_UNINTERRUPTED);

        //make sure that executeThread is blocking
        giveOthersAChance();
        executeThread.assertIsStarted();

        //interrupt the executingThread
        executeThread.interrupt();
        joinAll(executeThread);

        executeThread.assertIsInterruptedByException();
        assertActualPoolSize(poolsize);
        assertIsRunning();
        secondTask.assertIsNew();
    }

    private SleepingRunnable spawned_placeTask(long durationMs) {
        SleepingRunnable task = new SleepingRunnable(durationMs);
        ExecuteThread executeThread = scheduleExecute(task, START_UNINTERRUPTED);
        joinAll(executeThread);
        executeThread.assertIsTerminatedNormally();
        return task;
    }

    //synchronous quueue

    //there are no idle workers, but there is space in the queue

    public void testSuccess_noIdleWorkersButQueueHasSpace() {
        newStartedBlockingExecutor(10, 0);

        SleepingRunnable task = spawned_placeTask(DELAY_EON_MS);

        giveOthersAChance();
        assertActualPoolSize(0);
        assertTasksOnWorkQueue(task);
        assertIsRunning();
        task.assertIsNew();
    }

    //there are idle workers but there is no queue capacity (synchronousqueue is used)
    public void testSucccess_idleWorkers() throws InterruptedException {
        int poolsize = 10;
        newStartedBlockingExecutor(0, poolsize);

        SleepingRunnable task = spawned_placeTask(DELAY_EON_MS);

        giveOthersAChance();
        assertActualPoolSize(poolsize);
        assertWorkQueueIsEmpty();
        assertIsRunning();
        task.assertIsStarted();
    }

    public void testWhileUnstarted() {
        newUnstartedBlockingExecutor(1, 1);
        assertExecuteIsRejected();
    }

    public void testExecuteShuttingDown() {
        newShuttingDownBlockingExecutor(DELAY_EON_MS);
        assertExecuteIsRejected();
    }

    public void testWhileShutdown() {
        newShutdownBlockingExecutor(1, 1);
        assertExecuteIsRejected();
    }

    private void assertExecuteIsRejected() {
        BlockingExecutorServiceState oldState = executor.getState();
        CountingRunnable task = new CountingRunnable();

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
        TestRunnable initialTask = new SleepingRunnable(DELAY_LONG_MS);
        spawned_assertExecute(initialTask);

        //now place the second task. This call blocks untill space is available.
        TestRunnable task = new SleepingRunnable(DELAY_LONG_MS);
        ExecuteThread executeThread = scheduleExecute(task, START_UNINTERRUPTED);
        giveOthersAChance();
        executeThread.assertIsStarted();

        //in the meanwhile we are going to shutdown the blockingexecutor
        spawned_assertShutdown();
        System.out.println("executor.shutdown is called");
        assertIsShuttingDown();

        //now check that the executeThread fails with a RejectedExecutionException because
        //the blockingexecutor is shutting down.
        joinAll(executeThread);
        executeThread.assertIsTerminatedNormally();
    }
}
