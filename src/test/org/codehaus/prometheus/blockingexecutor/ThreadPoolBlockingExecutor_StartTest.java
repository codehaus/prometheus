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
        int poolsize = 10;
        newStartedBlockingExecutor(1,poolsize);

        start();

        assertIsRunning();
        assertActualPoolSize(poolsize);
        threadFactory.assertCreatedCount(poolsize);
        threadFactory.assertAllThreadsAlive();
    }

    public void testStartWhileStarted() {
        int poolsize = 10;
        newStartedBlockingExecutor(1,poolsize);

        start();

        assertIsRunning();
        assertActualPoolSize(poolsize);
        threadFactory.assertCreatedCount(poolsize);
        threadFactory.assertAllThreadsAlive();
    }

    private void start() {
        StartThread startThread = scheduleStart();
        joinAll(startThread);
        startThread.assertIsTerminatedWithoutThrowing();
    }

    public void testStartWhileShuttingDown() {
        newShuttingDownBlockingExecutor(DELAY_MEDIUM_MS);
        assertStartIsIllegal();
    }

    public void testStartWhileShutdown() {
        newShutdownBlockingExecutor(1,1);
        assertStartIsIllegal();
    }

    public void assertStartIsIllegal() {
        BlockingExecutorServiceState oldState = executor.getState();
        int oldpoolsize = executor.getThreadPool().getActualPoolSize();

        StartThread startThread = scheduleStart();
        joinAll(startThread);
        startThread.assertIsTerminatedWithThrowing(IllegalStateException.class);

        assertEquals(oldState,executor.getState());
        assertActualPoolSize(oldpoolsize);
    }
}
