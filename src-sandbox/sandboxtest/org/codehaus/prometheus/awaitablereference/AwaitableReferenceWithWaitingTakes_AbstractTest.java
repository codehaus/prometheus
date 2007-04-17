/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.awaitablereference;

import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.waitpoint.CloseableWaitpoint;
import org.codehaus.prometheus.waitpoint.Waitpoint;

public class AwaitableReferenceWithWaitingTakes_AbstractTest extends ConcurrentTestCase {

    public void testConstrutor() {
        try {
            new AwaitableReferenceWithWaitingTakes(null, new CloseableWaitpoint());
            fail();
        } catch (NullPointerException ex) {
            assertTrue(true);
        }

        try {
            new AwaitableReferenceWithWaitingTakes(new DefaultAwaitableReference(), null);
            fail();
        } catch (NullPointerException ex) {
            assertTrue(true);
        }

        AwaitableReference target = new DefaultAwaitableReference();
        Waitpoint waitpoint = new CloseableWaitpoint();
        AwaitableReferenceWithWaitingTakes awaitableRef = new AwaitableReferenceWithWaitingTakes(target, waitpoint);
        assertSame(target, awaitableRef.getTarget());
        assertSame(waitpoint, awaitableRef.getWaitpoint());
    }
}
