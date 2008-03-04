/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.sleepMs;
import org.codehaus.prometheus.concurrenttesting.Delays;
import org.codehaus.prometheus.concurrenttesting.TestCallable;
import org.codehaus.prometheus.concurrenttesting.TestRunnable;
import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.exceptionhandler.NoOpExceptionHandler;

import java.awt.image.ImagingOpException;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Vector;
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

    public void testExceptionThrownByHandlerIsDiscarded() throws InterruptedException {                        
        //the handler throws an exception when it is caught
        RuntimeException handlerEx = new RuntimeException();
        ThrowingExceptionHandler handler = new ThrowingExceptionHandler(handlerEx);
        newStartedThreadpool(1);
        threadpool.setExceptionHandler(handler);

        //the initial task that causes an exception
        Exception initialEx  = new ImagingOpException("");
        TestCallable problemTask = new TestCallable(initialEx);
        workQueue.put(problemTask);

        //an extra task to see that the worker thread is still working
        TestCallable trailingTask = new TestCallable(true);
        workQueue.put(trailingTask);

        giveOthersAChance(Delays.MEDIUM_MS);

        assertIsRunning();
        problemTask.assertExecutedOnce();
        trailingTask.assertExecutedOnce();
        threadPoolThreadFactory.assertCreatedAndAliveCount(1);
        handler.assertHandledExceptions(initialEx);
    }

    class ThrowingExceptionHandler implements ExceptionHandler{

        private final List<Exception> handledExceptions = new Vector<Exception>();
        private final RuntimeException exception;

        public ThrowingExceptionHandler(RuntimeException exception) {
            this.exception = exception;
        }

        public void handle(Exception ex) {
            handledExceptions.add(ex);
            throw exception;
        }

        public void assertHandledExceptions(Exception... exceptions){
            assertEquals(asList(exceptions), handledExceptions);
        }
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

        sleepMs(Delays.MEDIUM_MS);
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
        newForcedShuttingdownThreadpool(3, Delays.MEDIUM_MS);
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
