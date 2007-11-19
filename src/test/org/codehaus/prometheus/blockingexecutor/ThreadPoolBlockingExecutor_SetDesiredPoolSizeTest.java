/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.concurrenttesting.Delays;

/**
 * Unittests {@link ThreadPoolBlockingExecutor#setDesiredPoolSize(int)}
 *
 * @author Peter Veentjer
 */
public class ThreadPoolBlockingExecutor_SetDesiredPoolSizeTest extends ThreadPoolBlockingExecutor_AbstractTest {

    public void testIllegalArgument() {
        newStartedBlockingExecutor();
        int oldDesiredPoolSize = executor.getDesiredPoolSize();
        int oldActualPoolSize = executor.getActualPoolSize();

        spawned_assertSetDesiredPoolSizeThrowsException(-1, IllegalArgumentException.class);

        assertActualPoolSize(oldActualPoolSize);
        assertDesiredPoolSize(oldDesiredPoolSize);
        assertIsRunning();
    }

    public void testWhileUnstarted() {
        newUnstartedBlockingExecutor(100, 1);

        int oldDesiredPoolSize = executor.getDesiredPoolSize();

        int newPoolsize = oldDesiredPoolSize + 10;
        spawned_assertSetDesiredPoolSize(newPoolsize);

        assertActualPoolSize(0);
        assertDesiredPoolSize(newPoolsize);
        assertIsUnstarted();
    }

    public void testWhileRunning_emptyPool() {
        testWhileRunning(0);
    }

    public void testWhileRunning_nonEmptyPool() {
        testWhileRunning(2);
    }

    public void testWhileRunning(int poolsize) {
        newStartedBlockingExecutor(100, poolsize);

        int newPoolsize = poolsize + 3;
        spawned_assertSetDesiredPoolSize(newPoolsize);

        assertActualPoolSize(newPoolsize);
        assertDesiredPoolSize(newPoolsize);
        assertIsRunning();
    }

    public void testWhileShuttingdown() {
        newShuttingdownBlockingExecutor(Delays.EON_MS);
        assertChangeInDesiredPoolsizeIsRejected();
    }

    public void testWhileForcedShuttingdown() {
        newForcedShuttingdownBlockingExecutor(Delays.LONG_MS,3);
        assertChangeInDesiredPoolsizeIsRejected();
    }

    public void testWhileShutdown() {
        newShutdownBlockingExecutor(100, 3);
        assertChangeInDesiredPoolsizeIsRejected();
    }

    private void assertChangeInDesiredPoolsizeIsRejected() {
        int oldDesiredPoolSize = executor.getDesiredPoolSize();
        int newDesiredPoolSize = oldDesiredPoolSize+3;
        int oldActualPoolSize = executor.getActualPoolSize();
        BlockingExecutorServiceState oldState = executor.getState();

        spawned_assertSetDesiredPoolSizeThrowsException(newDesiredPoolSize, IllegalStateException.class);

        assertActualPoolSize(oldActualPoolSize);
        assertDesiredPoolSize(oldDesiredPoolSize);
        assertHasState(oldState);
    }
}
