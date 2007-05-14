/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.testsupport.CountingRunnable;
import org.codehaus.prometheus.testsupport.DummyRunnable;
import org.codehaus.prometheus.testsupport.SleepingRunnable;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unittests the {@link ThreadPoolBlockingExecutor#tryExecute(Runnable, long, TimeUnit)} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadpoolBlockingExecutor_TimedTryExecuteRunnableTest extends ThreadPoolBlockingExecutor_AbstractTest {

    public void testArguments() throws TimeoutException, InterruptedException {
        newStartedBlockingExecutor();
        try {
            executor.tryExecute(null, 1, TimeUnit.SECONDS);
            fail();
        } catch (NullPointerException ex) {
        }

        try {
            executor.tryExecute(new DummyRunnable(), 1, null);
            fail();
        } catch (NullPointerException ex) {
        }
    }

    public void testNegativeTimeout() throws InterruptedException {
        newStartedBlockingExecutor();
        try {
            executor.tryExecute(new DummyRunnable(), -1, TimeUnit.SECONDS);
            fail();
        } catch (TimeoutException ex) {
        }
    }

    public void testTimeout() {
        newStartedBlockingExecutor(0,0);

        CountingRunnable task = new CountingRunnable();
        TryExecuteThread executeThread = scheduleTryExecute(task,DELAY_SMALL_MS);
        joinAll(executeThread);

        executeThread.assertIsTimedOut();
        task.assertNotExecuted();
    }

    public void testInterruptedWhileWaiting() {
        newStartedBlockingExecutor(0,0);
        CountingRunnable task = new CountingRunnable();

        TryExecuteThread executeThread = scheduleTryExecute(task,DELAY_EON_MS);

        giveOthersAChance();
        executeThread.assertIsStarted();

        executeThread.interrupt();
        joinAll(executeThread);
        executeThread.assertIsInterruptedByException();

        giveOthersAChance();
        task.assertNotExecuted();
        assertIsRunning();
    }

    public void testSomeWaitingNeeded(){
        newStartedBlockingExecutor(0,1);

        SleepingRunnable sleepingRunnable = new SleepingRunnable(DELAY_MEDIUM_MS);
        ExecuteThread executeThread = scheduleExecute(sleepingRunnable,START_UNINTERRUPTED);
        joinAll(executeThread);
        executeThread.assertIsTerminatedWithoutThrowing();

        CountingRunnable task = new CountingRunnable();
        TryExecuteThread tryExecuteThread = scheduleTryExecute(task,DELAY_EON_MS);
        giveOthersAChance();
        tryExecuteThread.assertIsStarted();

        joinAll(tryExecuteThread);
        tryExecuteThread.assertIsSuccess();

        sleepMs(DELAY_MEDIUM_MS);
        task.assertExecutedOnce();
        assertIsRunning();
    }

    public void testNoWaitingNeeded() {
        newStartedBlockingExecutor(0,1);
        CountingRunnable task = new CountingRunnable();

        TryExecuteThread executeThread = scheduleTryExecute(task, 0);
        joinAll(executeThread);

        executeThread.assertIsSuccess();

        giveOthersAChance();
        task.assertExecutedOnce();
        assertDesiredPoolSize(1);
        assertActualPoolSize(1);
        assertIsRunning();
    }

    public void testExecuteWhenUnstarted() {
        newUnstartedBlockingExecutor(1,1);
        assertExecuteIsRejected();
    }

    public void testExecuteWhileShuttingDown() {
        newShuttingDownBlockingExecutor(DELAY_EON_MS);
        assertExecuteIsRejected();
    }

    public void testExecuteWhileShutdown() {
        newShutdownBlockingExecutor(1,1);
        assertExecuteIsRejected();
    }

    private void assertExecuteIsRejected() {
        BlockingExecutorServiceState oldState = executor.getState();
        CountingRunnable task = new CountingRunnable();

        TryExecuteThread executeThread = scheduleTryExecute(task, DELAY_SMALL_MS);
        joinAll(executeThread);

        executeThread.assertIsTerminatedWithThrowing(RejectedExecutionException.class);
        assertEquals(oldState, executor.getState());
        task.assertNotExecuted();
    }
}
