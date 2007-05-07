/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.testsupport.CountingRunnable;
import org.codehaus.prometheus.testsupport.DummyRunnable;

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
            assertTrue(true);
        }

        try {
            executor.tryExecute(new DummyRunnable(), 1, null);
            fail();
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testNegativeTimeout() throws InterruptedException {
        newStartedBlockingExecutor();
        try {
            executor.tryExecute(new DummyRunnable(), -1, TimeUnit.SECONDS);
            fail();
        } catch (TimeoutException ex) {
            assertTrue(true);
        }
    }

    public void testTimeout() {
        fail();
    }

    public void testInterruptedWhileWaiting() {
        newStartedBlockingExecutor();
        CountingRunnable task = new CountingRunnable();

        TryExecuteThread executeThread = scheduleDelayedTryExecute(task,1000, 100);
        executeThread.interrupt();
        joinAll(executeThread);

        executeThread.assertIsInterruptedByException();

        sleepMs(100);
        task.assertExecutedOnce();
        assertDesiredPoolSize(1);
        assertActualPoolSize(1);
        assertIsRunning();
    }

    public void testSuccess() {
        newStartedBlockingExecutor();
        CountingRunnable task = new CountingRunnable();

        TryExecuteThread executeThread = scheduleTryExecute(task, 100);
        joinAll(executeThread);

        executeThread.assertIsSuccess();

        sleepMs(100);
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
        assertEquals(oldState, executeThread.getState());
        task.assertNotExecuted();
    }
}
