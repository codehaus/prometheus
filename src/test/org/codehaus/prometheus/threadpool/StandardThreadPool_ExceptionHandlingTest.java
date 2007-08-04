/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.exceptionhandler.NullExceptionHandler;
import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;
import static org.codehaus.prometheus.testsupport.TestUtil.sleepMs;
import org.codehaus.prometheus.testsupport.ThrowingRunnable;

import java.util.concurrent.Executors;

/**
 * Unittests the {@link StandardThreadPool} {@link org.codehaus.prometheus.exceptionhandler.ExceptionHandler}
 * functionality.
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool_ExceptionHandlingTest extends StandardThreadPool_AbstractTest {

    public void testSetNullExceptionHandler() {
        newStartedThreadpool();

        threadpool.setExceptionHandler(null);
        assertTrue(threadpool.getExceptionHandler() instanceof NullExceptionHandler);
    }

    /**
     * every time the task is executed, an exception is thrown.
     * it also tests that an exception doesn't corrupt the threadpool (so workers work
     * again after receiving an exception
     */
    public void testHandlerIsCalledForRunWorkAndNonInterruptedException() {
        int poolsize = 3;
        newStartedThreadpool(poolsize);

        int errorcount = 30;
        createBunchOfProblemTasks(errorcount);

        sleepMs(DELAY_LONG_MS);
        threadPoolThreadFactory.assertCreatedAndAliveCount(poolsize);
        threadPoolExceptionHandler.assertCount(errorcount);
        assertActualPoolsize(poolsize);
        assertDesiredPoolsize(poolsize);
        assertIsRunning();
    }

    public void placeTask(Runnable runnable){
        taskQueue.add(Executors.callable(runnable,true));
    }

    private void createBunchOfProblemTasks(int errorcount) {
        for (int k = 0; k < errorcount; k++)
            placeTask(new ThrowingRunnable());
    }

    public void testHandlerIsNotCalledForGetWorkAndInterruptedException() {
        int poolsize = 3;
        newStartedThreadpool(poolsize);

        spawned_assertShutdownNow();

        //make sure that no exception has been thrown.
        giveOthersAChance();
        threadPoolThreadFactory.assertCreatedAndTerminatedCount(poolsize);
        threadPoolExceptionHandler.assertCount(0);
    }

    public void test_getWork_otherException() {
        //todo
    }

    public void test_getShuttingdownWork_otherException() {
        //todo
    }

    public void testSet_whileUnstarted() {
        newUnstartedThreadPool();
        assertSetHandlerWorks();
    }

    public void testSet_whileStarted() {
        newStartedThreadpool();
        assertSetHandlerWorks();
    }

    public void testSet_whileShuttingDown() {
        newShuttingdownThreadpool(3, DELAY_EON_MS);
        assertSetHandlerWorks();
    }

    public void testSetWhileForcedShuttingdown() {
        newForcedShuttingdownThreadpool(3, DELAY_LONG_MS);
        assertSetHandlerWorks();
    }

    public void testSetWhileShutdown() {
        newShutdownThreadpool();
        assertSetHandlerWorks();
    }

    public void assertSetHandlerWorks() {
        ExceptionHandler handler = new NullExceptionHandler();
        threadpool.setExceptionHandler(handler);
        assertSame(handler, threadpool.getExceptionHandler());
    }
}
