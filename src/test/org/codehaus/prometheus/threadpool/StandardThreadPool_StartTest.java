/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.joinAll;
import org.codehaus.prometheus.concurrenttesting.Delays;

/**
 * Unittests {@link StandardThreadPool#start()} method.
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool_StartTest extends StandardThreadPool_AbstractTest {

    public void testWhileUnstartedAndNoDefaultWorkJob() {
        newUnstartedThreadPoolWithoutDefaultJob(10);

        spawned_startCausesIllegalStateException();

        assertIsUnstarted();
        threadPoolThreadFactory.assertCreatedCount(0);
        threadPoolExceptionHandler.assertNoErrors();
    }

    private void spawned_startCausesIllegalStateException() {
        StartThread startThread = scheduleStart();
        joinAll(startThread);
        startThread.assertIsTerminatedWithThrowing(IllegalStateException.class);
    }

    public void testWhileUnstarted() {
        int poolsize = 3;
        newUnstartedThreadPool(poolsize);

        spawned_start();

        assertIsRunning();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
        threadPoolThreadFactory.assertAllAreAlive();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testStarted() {
        int poolsize = 3;
        newStartedThreadpool(poolsize);

        spawned_start();

        assertIsRunning();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
        threadPoolThreadFactory.assertAllAreAlive();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileShuttingdown() {
        int poolsize = 3;
        newShuttingdownThreadpool(poolsize, Delays.EON_MS);

        spawned_startCausesIllegalStateException();

        assertIsShuttingdown();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
        threadPoolThreadFactory.assertAllAreAlive();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileForcedShuttingdown(){
        int poolsize = 3;
        newForcedShuttingdownThreadpool(poolsize, Delays.LONG_MS);

        spawned_startCausesIllegalStateException();

        assertIsForcedShuttingdown();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
        threadPoolThreadFactory.assertAllAreAlive();
        threadPoolExceptionHandler.assertNoErrors();
    }
    
    public void testWhileShutdown() {
        newShutdownThreadpool();
        int oldpoolsize = threadPoolThreadFactory.getThreadCount();

        spawned_startCausesIllegalStateException();

        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(oldpoolsize);
        threadPoolThreadFactory.assertAllAreNotAlive();
        threadPoolExceptionHandler.assertNoErrors();
    }
}
