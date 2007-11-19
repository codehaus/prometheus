/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.concurrenttesting.SleepingRunnable;
import org.codehaus.prometheus.concurrenttesting.TestRunnable;
import org.codehaus.prometheus.concurrenttesting.Delays;
import static org.codehaus.prometheus.concurrenttesting.TestSupport.newSleepingRunnable;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unittests the {@link ThreadPoolBlockingExecutor#tryExecute(Runnable,long,TimeUnit)} method.
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
            executor.tryExecute(new TestRunnable(), 1, null);
            fail();
        } catch (NullPointerException ex) {
        }
    }

    public void testNegativeTimeout() throws InterruptedException {
        newStartedBlockingExecutor();
        try {
            executor.tryExecute(new TestRunnable(), -1, TimeUnit.SECONDS);
            fail();
        } catch (TimeoutException ex) {
        }
    }

    public void testTimeout() {
        newStartedBlockingExecutor(0, 0);

        TestRunnable task = new TestRunnable();
        TryExecuteThread executeThread = scheduleTryExecute(task, Delays.SMALL_MS);
        joinAll(executeThread);

        executeThread.assertIsTimedOut();
        task.assertNotExecuted();
    }

    public void testInterruptedWhileWaiting() {
        newStartedBlockingExecutor(0, 0);
        TestRunnable task = new TestRunnable();

        TryExecuteThread executeThread = scheduleTryExecute(task, Delays.EON_MS);

        giveOthersAChance();
        executeThread.assertIsStarted();

        executeThread.interrupt();
        joinAll(executeThread);
        executeThread.assertIsTerminatedByInterruptedException();

        giveOthersAChance();
        task.assertNotExecuted();
        assertIsRunning();
    }

    public void testSomeWaitingNeeded() {
        newStartedBlockingExecutor(0, 1);

        SleepingRunnable sleepingRunnable = newSleepingRunnable(Delays.MEDIUM_MS);
        spawned_execute(sleepingRunnable);

        TestRunnable task = new TestRunnable();
        TryExecuteThread tryExecuteThread = scheduleTryExecute(task, Delays.EON_MS);
        giveOthersAChance();
        tryExecuteThread.assertIsStarted();

        joinAll(tryExecuteThread);
        tryExecuteThread.assertIsSuccess();

        sleepMs(Delays.MEDIUM_MS);
        task.assertExecutedOnce();
        assertIsRunning();
    }

    public void testNoWaitingNeeded() {
        newStartedBlockingExecutor(0, 1);
        TestRunnable task = new TestRunnable();

        TryExecuteThread executeThread = scheduleTryExecute(task, 0);
        joinAll(executeThread);
        executeThread.assertIsSuccess();

        giveOthersAChance();
        task.assertExecutedOnce();
        assertDesiredPoolSize(1);
        assertActualPoolSize(1);
        assertIsRunning();
    }

    public void testWhileUnstarted() {
        newUnstartedBlockingExecutor(1, 1);
        assertExecuteIsRejected();
    }

    public void testWhileShuttingdown() {
        newShuttingdownBlockingExecutor(Delays.EON_MS);
        assertExecuteIsRejected();
    }

    public void testWhileForcedShuttingdown(){
        newForcedShuttingdownBlockingExecutor(Delays.LONG_MS,3);
        assertExecuteIsRejected();
    }

    public void testWhileShutdown() {
        newShutdownBlockingExecutor(1, 1);
        assertExecuteIsRejected();
    }

    private void assertExecuteIsRejected() {
        BlockingExecutorServiceState oldState = executor.getState();
        int oldThreadCount = threadFactory.getThreadCount();
        TestRunnable task = new TestRunnable();

        TryExecuteThread executeThread = scheduleTryExecute(task, Delays.SMALL_MS);
        joinAll(executeThread);
        executeThread.assertIsRejected();

        assertHasState(oldState);
        threadFactory.assertCreatedAndAliveCount(oldThreadCount);
        task.assertNotExecuted();
    }
}
