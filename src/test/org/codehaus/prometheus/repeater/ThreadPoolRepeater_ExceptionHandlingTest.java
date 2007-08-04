/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.exceptionhandler.TracingExceptionHandler;
import static org.codehaus.prometheus.testsupport.TestUtil.sleepMs;
import org.codehaus.prometheus.testsupport.ThrowingRunnable;

/**
 * Unittests the exception handling behavior of the ThreadPoolRepeater.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_ExceptionHandlingTest extends ThreadPoolRepeater_AbstractTest {

    public void testExceptionHandler() {
        newRunningStrictRepeater();

        ExceptionHandler handler = new TracingExceptionHandler();
        repeater.setExceptionHandler(handler);
        assertSame(handler, repeater.getExceptionHandler());
    }

    //with a strict repeater
    public void testRunningTaskCausesRuntimeException_strict() throws InterruptedException {
        testRunningTaskCausesRuntimeException(true);
    }

    //with a relaxed repeater
    public void testRunningTaskCausesRuntimeException_relaxed() throws InterruptedException {
        testRunningTaskCausesRuntimeException(false);
    }

    public void testRunningTaskCausesRuntimeException(boolean strict) throws InterruptedException {
        newRunningRepeater(strict, 1);

        ThrowingRunnable task = new ThrowingRunnable();
        Repeatable repeatable = new RepeatableRunnable(task);
        spawned_repeat(repeatable);

        sleepMs(DELAY_LONG_MS);
        task.assertExecutedOnceOrMore();
        assertHasRepeatable(repeatable);
        assertIsRunning();

        //todo: testing that exception is caught
    }

}
