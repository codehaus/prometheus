/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.exceptionhandler.TracingExceptionHandler;
import org.codehaus.prometheus.concurrenttesting.TestRunnable;
import org.codehaus.prometheus.concurrenttesting.Delays;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;

/**
 * Unittest the Exception handling functionality of the ThreadPoolBlockingExecutor.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor_ExceptionHandlingTest extends ThreadPoolBlockingExecutor_AbstractTest {
    private TracingExceptionHandler exceptionHandler;

    public void testSetExceptionHandler() {
        executor = new ThreadPoolBlockingExecutor(1);

        ExceptionHandler handler = new TracingExceptionHandler();
        executor.setExceptionHandler(handler);
        assertSame(handler, executor.getExceptionHandler());
    }

    public void testExceptionHandlerIsUsed() {
        newStartedBlockingExecutor(1, 1);
        exceptionHandler = new TracingExceptionHandler();
        executor.setExceptionHandler(exceptionHandler);

        //do a few throwing an non throwing calls 
        assertThrowingTaskActivatesExceptionHandler();
        assertNormalTaskDoesntActivateExceptionHandler();
        assertThrowingTaskActivatesExceptionHandler();
        assertNormalTaskDoesntActivateExceptionHandler();
        assertNormalTaskDoesntActivateExceptionHandler();
        assertThrowingTaskActivatesExceptionHandler();
        assertNormalTaskDoesntActivateExceptionHandler();
    }

    private void assertNormalTaskDoesntActivateExceptionHandler() {
        int oldExceptionCount = exceptionHandler.getCount();
        TestRunnable task = new TestRunnable();
        spawned_execute(task);
        giveOthersAChance(Delays.MEDIUM_MS);
        task.assertExecutedOnce();
        assertEquals(oldExceptionCount, exceptionHandler.getCount());
    }

    private void assertThrowingTaskActivatesExceptionHandler() {
        StringIndexOutOfBoundsException ex = new StringIndexOutOfBoundsException();
        int oldExceptionCount = exceptionHandler.getCount(ex.getClass());
        TestRunnable task = new TestRunnable(ex);
        spawned_execute(task);
        giveOthersAChance(Delays.MEDIUM_MS);
        task.assertExecutedOnce();
        exceptionHandler.assertErrorCountAndNoOthers(ex.getClass(), oldExceptionCount + 1);
    }

    public void testFailingExceptionHandlerDoesntCorruptExecutor() {
        fail();
    }

    //make sure that a throwable that is not an exception is not caugtht
    public void testErrorIsNotCaught() {
        fail();
    }
}
