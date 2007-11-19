/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.exceptionhandler.NoOpExceptionHandler;
import org.codehaus.prometheus.concurrenttesting.TestRunnable;
import org.codehaus.prometheus.concurrenttesting.Delays;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.sleepMs;

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

        try {
            threadpool.setExceptionHandler(null);
            fail();
        } catch (NullPointerException ex) {
        }
    }

    public void testExceptionThrownByHandlerIsDiscarded(){
        //todo
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

        sleepMs(Delays.LONG_MS);
        threadPoolThreadFactory.assertCreatedAndAliveCount(poolsize);
        threadPoolExceptionHandler.assertCount(errorcount);
        assertActualPoolsize(poolsize);
        assertDesiredPoolsize(poolsize);
        assertIsRunning();
    }

    public void placeTask(Runnable runnable) {
        workQueue.add(Executors.callable(runnable, true));
    }

    private void createBunchOfProblemTasks(int errorcount) {
        for (int k = 0; k < errorcount; k++)
            placeTask(new TestRunnable(new RuntimeException()));
    }

    public void testHandlerIsNotCalledForGetWorkAndInterruptedException() {
        int poolsize = 3;
        newStartedThreadpool(poolsize);

        spawned_shutdownNow();

        //make sure that no exception has been thrown.
        giveOthersAChance();
        threadPoolThreadFactory.assertCreatedAndNotAliveCount(poolsize);
        threadPoolExceptionHandler.assertCount(0);
    }

    public void test_getWorkThrowsException() {
        int poolsize = 3;
        newStartedThreadpool(poolsize);

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
        newShuttingdownThreadpool(3, Delays.EON_MS);
        assertSetHandlerWorks();
    }

    public void testSetWhileForcedShuttingdown() {
        newForcedShuttingdownThreadpool(3, Delays.LONG_MS);
        assertSetHandlerWorks();
    }

    public void testSetWhileShutdown() {
        newShutdownThreadpool();
        assertSetHandlerWorks();
    }

    public void assertSetHandlerWorks() {
        ExceptionHandler handler = new NoOpExceptionHandler();
        threadpool.setExceptionHandler(handler);
        assertSame(handler, threadpool.getExceptionHandler());
    }
}
