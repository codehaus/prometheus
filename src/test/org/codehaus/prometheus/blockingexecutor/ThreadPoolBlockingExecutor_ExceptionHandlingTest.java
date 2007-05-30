package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.exceptionhandler.TracingExceptionHandler;
import org.codehaus.prometheus.testsupport.ThrowingRunnable;
import org.codehaus.prometheus.testsupport.CountingRunnable;

/**
 * Unittest the Exception handling functionality of the ThreadPoolBlockingExecutor.
 *
 * @author Peter Veentjer.
 *
 * allshare: leo bornstein: different places to work
 * aix, websphere, sprin, hibernate, groovy on grails
 *
 * mike lesna
 * 
 */
public class ThreadPoolBlockingExecutor_ExceptionHandlingTest extends ThreadPoolBlockingExecutor_AbstractTest{
    private TracingExceptionHandler exceptionHandler;

    public void testSetExceptionHandler(){
        executor = new ThreadPoolBlockingExecutor(1);

        ExceptionHandler handler = new TracingExceptionHandler();
        executor.setExceptionHandler(handler);
        assertSame(handler,executor.getExceptionHandler());
    }

    public void testExceptionHandlerIsUsed(){
        newStartedBlockingExecutor(1,1);
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

    private void assertNormalTaskDoesntActivateExceptionHandler(){
        int oldExceptionCount = exceptionHandler.getCount();
        CountingRunnable task = new CountingRunnable();
        _tested_execute(task);
        giveOthersAChance(DELAY_MEDIUM_MS);
        task.assertExecutedOnce();
        assertEquals(oldExceptionCount,exceptionHandler.getCount());
    }

    private void assertThrowingTaskActivatesExceptionHandler() {
        StringIndexOutOfBoundsException ex = new StringIndexOutOfBoundsException();
        int oldExceptionCount = exceptionHandler.getCount(ex.getClass());
        ThrowingRunnable task = new ThrowingRunnable(ex);
        _tested_execute(task);
        giveOthersAChance(DELAY_MEDIUM_MS);
        task.assertExecutedOnce();
        exceptionHandler.assertCountAndNoOthers(ex.getClass(),oldExceptionCount+1);
    }

    public void testFailingExceptionHandlerDoesntCorruptExecutor(){
        //todo
    }

    //make sure that a throwable that is not an exception is not caugtht
    public void testThrowableIsNotCaught(){
        //todo
    }
}
