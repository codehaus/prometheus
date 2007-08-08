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

    public void testWhileUnstarted_emptyPool() {
        testWhileUnstarted(0);
    }

    public void testWhileUnstarted_nonEmptyPool() {
        testWhileUnstarted(3);
    }

    public void testWhileUnstarted(int poolsize) {
        newStartedBlockingExecutor(1, poolsize);

        spawned_start();

        assertIsRunning();
        assertActualPoolSize(poolsize);
        threadFactory.assertCreatedCount(poolsize);
        threadFactory.assertAllThreadsAreAlive();
    }

    public void testWhileRunning() {
        int poolsize = 3;
        newStartedBlockingExecutor(1, poolsize);

        spawned_start();

        assertIsRunning();
        assertActualPoolSize(poolsize);
        threadFactory.assertCreatedCount(poolsize);
        threadFactory.assertAllThreadsAreAlive();
    }

    public void testWhileShuttingdown() {
        newShuttingdownBlockingExecutor(DELAY_MEDIUM_MS);
        assertStartIsIllegal();
    }

    public void testWhileForcedShuttingdown(){
        newForcedShuttingdownBlockingExecutor(DELAY_LONG_MS,3);
        assertStartIsIllegal();
    }

    public void testWhileShutdown() {
        newShutdownBlockingExecutor(1, 1);
        assertStartIsIllegal();
    }

    public void assertStartIsIllegal() {
        BlockingExecutorServiceState oldState = executor.getState();
        int oldpoolsize = executor.getThreadPool().getActualPoolSize();

        StartThread startThread = scheduleStart();
        joinAll(startThread);
        startThread.assertIsTerminatedWithThrowing(IllegalStateException.class);

        assertEquals(oldState, executor.getState());
        assertActualPoolSize(oldpoolsize);
    }
}
