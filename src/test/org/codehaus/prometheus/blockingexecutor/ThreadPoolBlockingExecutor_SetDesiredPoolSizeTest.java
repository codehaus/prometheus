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

    public void testWhileRunning_poolsizeDecreases() {
        fail();
    }

    public void testWhileRunning_poolsizeIncreases() {
        fail();
    }

    public void testWhileShuttingdown() {
        newShuttingdownBlockingExecutor(Delays.EON_MS);
        assertChangeInDesiredPoolsizeIsRejected();
    }

    public void testWhileForcedShuttingdown() {
        newForcedShuttingdownBlockingExecutor(Delays.LONG_MS, 3);
        assertChangeInDesiredPoolsizeIsRejected();
    }

    public void testWhileShutdown() {
        newShutdownBlockingExecutor();
        assertChangeInDesiredPoolsizeIsRejected();
    }

    private void assertChangeInDesiredPoolsizeIsRejected() {
        int oldDesiredPoolSize = executor.getDesiredPoolSize();
        int newDesiredPoolSize = oldDesiredPoolSize + 3;
        int oldActualPoolSize = executor.getActualPoolSize();
        BlockingExecutorServiceState oldState = executor.getState();

        spawned_assertSetDesiredPoolSizeThrowsException(newDesiredPoolSize, IllegalStateException.class);

        assertActualPoolSize(oldActualPoolSize);
        assertDesiredPoolSize(oldDesiredPoolSize);
        assertHasState(oldState);
    }
}
