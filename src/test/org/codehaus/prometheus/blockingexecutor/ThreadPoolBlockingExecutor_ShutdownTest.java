/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.testsupport.DummyRunnable;
import org.codehaus.prometheus.testsupport.NonInterruptableSleepingRunnable;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unittests the {@link ThreadPoolBlockingExecutor#shutdown()} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor_ShutdownTest extends ThreadPoolBlockingExecutor_AbstractTest {

    public void testNotStarted() {
        newUnstartedBlockingExecutor(1,1);
        executor.shutdown();
        assertIsShutdown();
    }

    public void testRunningWithoutRunningTask() {
        newStartedBlockingExecutor(1,1);
        executor.shutdown();
        Thread.yield();
        assertIsShutdown();
    }

    public void testRunningWithRunningTask() {
        newStartedBlockingExecutor(1,1,new NonInterruptableSleepingRunnable(500));
        executor.shutdown();
        Thread.yield();
        assertIsShuttingDown();

        sleepMs(510);
        assertIsShutdown();
    }

    public void testShutdownWhileShuttingDown() {
        newShuttingDownBlockingExecutor(1000);
        executor.shutdown();
        Thread.yield();
        assertIsShuttingDown();
        assertWontAcceptNewTasks();
    }

    public void testShutdownWhileShutdown() {
        newShutdownBlockingExecutor(1,1);
        executor.shutdown();
        assertIsShutdown();
        assertWontAcceptNewTasks();
    }

    public void assertWontAcceptNewTasks() {
        try {
            try {
                executor.execute(new DummyRunnable());
                fail();
            } catch (RejectedExecutionException ex) {
                assertTrue(true);
            }

            try {
                executor.tryExecute(new DummyRunnable(), 1, TimeUnit.MILLISECONDS);
                fail();
            } catch (RejectedExecutionException ex) {
                assertTrue(true);
            }
        } catch (InterruptedException e) {
            fail();
        } catch (TimeoutException ex) {
            fail();
        }
    }
}


