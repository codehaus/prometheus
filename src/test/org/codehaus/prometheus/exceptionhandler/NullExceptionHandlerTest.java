/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.exceptionhandler;

import org.codehaus.prometheus.concurrenttesting.ConcurrentTestCase;

/**
 * Unittests the {@link NoOpExceptionHandler}.
 *
 * @author Peter Veentjer.
 */
public class NullExceptionHandlerTest extends ConcurrentTestCase {

    //check it doesn't complain when a null is passed.
    public void testHandleNull() {
        NoOpExceptionHandler.INSTANCE.handle(null);
    }

    public void testHandleException() {
        NoOpExceptionHandler.INSTANCE.handle(new Exception());
    }
}
