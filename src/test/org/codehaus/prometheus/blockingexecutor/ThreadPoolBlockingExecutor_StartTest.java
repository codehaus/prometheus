/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.joinAll;
import org.codehaus.prometheus.testsupport.Delays;

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
        threadFactory.assertAllAreAlive();
    }

    public void testWhileRunning() {
        int poolsize = 3;
        newStartedBlockingExecutor(1, poolsize);

        spawned_start();

        assertIsRunning();
        assertActualPoolSize(poolsize);
        threadFactory.assertCreatedCount(poolsize);
        threadFactory.assertAllAreAlive();
    }

    public void testWhileShuttingdown() {
        newShuttingdownBlockingExecutor(Delays.MEDIUM_MS);
        assertStartIsIllegal();
    }

    public void testWhileForcedShuttingdown(){
        newForcedShuttingdownBlockingExecutor(Delays.LONG_MS,3);
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
