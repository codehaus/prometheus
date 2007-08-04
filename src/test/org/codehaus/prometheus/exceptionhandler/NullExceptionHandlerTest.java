/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.exceptionhandler;

import org.codehaus.prometheus.testsupport.ConcurrentTestCase;

/**
 * Unittests the {@link NullExceptionHandler}.
 *
 * @author Peter Veentjer.
 */
public class NullExceptionHandlerTest extends ConcurrentTestCase {

    //check it doesn't complain when a null is passed.
    public void testHandleNull() {
        NullExceptionHandler.INSTANCE.handle(null);
    }

    public void testHandleException() {
        NullExceptionHandler.INSTANCE.handle(new Exception());
    }
}
