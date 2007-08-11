/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.joinAll;
import org.codehaus.prometheus.testsupport.Delays;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unittests the {@link org.codehaus.prometheus.references.DefaultAwaitableReference#tryPut(Object,long,TimeUnit)}
 *
 * @author Peter Veentjer.
 */
public class DefaultAwaitableReference_TimedTryPutTest extends DefaultAwaitableReference_AbstractTest {

    public void testArguments() throws TimeoutException, InterruptedException {
        awaitableRef = new DefaultAwaitableReference<Integer>();
        try {
            awaitableRef.tryPut(10, 10, null);
            fail();
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testNegativeTimeout() throws InterruptedException {
        awaitableRef = new DefaultAwaitableReference<Integer>();

        try {
            awaitableRef.tryPut(10, -1, TimeUnit.MILLISECONDS);
            fail();
        } catch (TimeoutException e) {
            assertTrue(true);
        }
    }

    public void testStartInterrupted() {
        Integer oldRef = 10;
        awaitableRef = new DefaultAwaitableReference<Integer>(oldRef);

        Integer newRef = 20;
        TimedTryPutThread tryPutThread = scheduleTryPut(newRef, 1, START_INTERRUPTED);
        joinAll(tryPutThread);
        tryPutThread.assertSuccess(oldRef);

        assertHasReference(newRef);
    }

    public void testNotReturnedValueDoesntBlockTryPut() {
        testNotReturnedValueDoesntBlockTryPut(0);
        testNotReturnedValueDoesntBlockTryPut(Delays.SMALL_MS);
    }

    /**
     * Check that values that are lend, don't block putting new values
     */
    public void testNotReturnedValueDoesntBlockTryPut(long timeout) {
        Integer oldRef = 10;
        awaitableRef = new DefaultAwaitableReference<Integer>(oldRef);

        //first take a reference
        spawned_take(oldRef);

        //now put a reference, this should complete because put isn't blocked by takes
        Integer newRef = 20;
        spawned_tryPut(timeout, newRef, oldRef);
    }

    public void testSuccess_startFromNullValue() {
        testSuccess(null);
    }

    public void testSuccess_startFromNonNullValue() {
        testSuccess(new Integer(100));
    }

    public void testSuccess(Integer originalRef) {
        awaitableRef = new DefaultAwaitableReference<Integer>(originalRef);

        Integer newRef = originalRef == null ? 1 : originalRef + 1;
        spawned_tryPut(Delays.SMALL_MS, newRef, originalRef);
    }
}
