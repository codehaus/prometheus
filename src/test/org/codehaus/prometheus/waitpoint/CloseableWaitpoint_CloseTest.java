/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.waitpoint;

/**
 * Unittests the {@link org.codehaus.prometheus.waitpoint.CloseableWaitpoint#close()} method.
 *
 * @author Peter Veentjer.
 */
public class CloseableWaitpoint_CloseTest extends CloseableWaitpoint_AbstractTest {

    public void testFromClosedToClosed(){
        newClosedCloseableWaitpoint();
        waitpoint.close();
        assertIsClosed();
    }

    public void testFromOpenToClosed(){
        newOpenCloseableWaitpoint();
        waitpoint.close();
        assertIsClosed();
    }
}
