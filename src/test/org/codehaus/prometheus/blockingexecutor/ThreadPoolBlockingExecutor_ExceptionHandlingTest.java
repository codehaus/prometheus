package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.exceptionhandler.TracingExceptionHandler;

/**
 * Unittest the Exception handling functionality of the ThreadPoolBlockingExecutor.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor_ExceptionHandlingTest extends ThreadPoolBlockingExecutor_AbstractTest{

    public void testExceptionHandler(){
        executor = new ThreadPoolBlockingExecutor(1);

        ExceptionHandler handler = new TracingExceptionHandler();
        executor.setExceptionHandler(handler);
        assertSame(handler,executor.getExceptionHandler());
    }
}
