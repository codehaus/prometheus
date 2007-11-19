/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import org.codehaus.prometheus.concurrenttesting.ConcurrentTestCase;

public class AwaitableReferenceWithWaitingTakes_AbstractTest extends ConcurrentTestCase {

    public void testDummy(){}

    /*
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
    } */
}
