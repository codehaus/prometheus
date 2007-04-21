/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

/**
 * Unittests the {@link ThreadPoolBlockingExecutor#start()} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor_StartTest extends ThreadPoolBlockingExecutor_AbstractTest {

    public void testStartWhileNotStarted() {
        newStartedBlockingExecutor(1,1);
        executor.start();
        assertIsRunning();
    }

    public void testStartWhileStarted() {
        newStartedBlockingExecutor(1,1);
        executor.start();
        assertIsRunning();
    }

    public void testStartWhileShuttingDown() {
        newShuttingDownBlockingExecutor(1000);
        assertStartThrowsIllegalStateException();
    }

    public void testStartWhileShutdown() {
        newShutdownBlockingExecutor(1,1);
        assertStartThrowsIllegalStateException();
    }

    public void assertStartThrowsIllegalStateException() {
        BlockingExecutorServiceState oldState = executor.getState();

        try {
            executor.start();
            fail("IllegalStateException expected");
        } catch (IllegalStateException ex) {
            assertTrue(true);
        }

        assertEquals(oldState,executor.getState());
    }
}
