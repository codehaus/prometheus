/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.testsupport.CountingRunnable;
import org.codehaus.prometheus.testsupport.SleepingRunnable;

import java.util.concurrent.RejectedExecutionException;

//als je waitable executes wilt, moet je de queue size ook kunnen controleren.

/**
 * Unittests the {@link ThreadPoolBlockingExecutor#execute(Runnable)} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor_ExecuteRunnableTest extends ThreadPoolBlockingExecutor_AbstractTest {

    public void testArguments() throws InterruptedException {
        newStartedBlockingExecutor(1,1);

        try {
            executor.execute((Runnable) null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testInterruptedWhileWaiting() {
        fail();
    }

    public void testNeedsSomeWaiting(){
        newStartedBlockingExecutor(1,1,new SleepingRunnable(2* DELAY_SMALL_MS));

        stopwatch.start();
        CountingRunnable task = new CountingRunnable();
        ExecuteThread executeThread = scheduleExecute(task,false);

        Thread.yield();
        executeThread.assertIsStarted();


        stopwatch.stop();

        fail();
    }

    public void testSucccess() throws InterruptedException {
        newStartedBlockingExecutor();
        CountingRunnable task = new CountingRunnable();
        ExecuteThread t = scheduleExecute(task,false);

        joinAll(t);
        t.assertIsTerminated();
        assertIsRunning();
        assertActualPoolSize(1);

        sleepMs(100);

        assertIsRunning();
        assertWorkQueueIsEmpty();
        task.assertExecutedOnce();
    }

    public void testExecuteWhenUnstarted() {
        newUnstartedBlockingExecutor(1,1);
        assertExecuteIsRejected();
    }

    public void testExecuteWhileShuttingDown() {
        newShuttingDownBlockingExecutor(1000);
        assertExecuteIsRejected();
    }

    public void testExecuteWhileShutdown() {
        newShutdownBlockingExecutor(1,1);
        assertExecuteIsRejected();
    }

    private void assertExecuteIsRejected() {
        BlockingExecutorServiceState oldState = executor.getState();
        CountingRunnable task = new CountingRunnable();
        ExecuteThread t = scheduleExecute(task,false);
        joinAll(t);

        t.assertIsTerminatedWithThrowing(RejectedExecutionException.class);

        //give a worker thread the chance to runWork
        sleepMs(100);
        assertEquals(oldState, executor.getState());
        task.assertNotExecuted();
    }
}
