/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import org.codehaus.prometheus.testsupport.InterruptedFalse;
import org.codehaus.prometheus.testsupport.InterruptedTrue;
import org.codehaus.prometheus.testsupport.InterruptedTrueFalse;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unittests the {@link org.codehaus.prometheus.references.DefaultAwaitableReference#tryPut(Object,long,TimeUnit)}
 *
 * @author Peter Veentjer.
 */
public class DefaultAwaitableReference_TimedTryPutTest extends DefaultAwaitableReference_AbstractTests {

    @InterruptedTrueFalse
    public void testArguments() throws TimeoutException, InterruptedException {
        awaitableRef = new DefaultAwaitableReference<Integer>();
        try {
            awaitableRef.tryPut(10, 10, null);
            fail();
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    @InterruptedTrueFalse
    public void testNegativeTimeout() throws InterruptedException {
        awaitableRef = new DefaultAwaitableReference<Integer>();

        try {
            awaitableRef.tryPut(10, -1, TimeUnit.MILLISECONDS);
            fail();
        } catch (TimeoutException e) {
            assertTrue(true);
        }
    }

    @InterruptedTrue
    public void testStartInterrupted() {
        Integer oldRef = 10;
        awaitableRef = new DefaultAwaitableReference<Integer>(oldRef);
        Integer newRef = 20;

        tested_put(newRef, oldRef);

        assertHasReference(newRef);
    }

    @InterruptedFalse
    public void testNotReturnedValueDoesntBlockTryPut() {
        testNotReturnedValueDoesntBlockTryPut(0);
        testNotReturnedValueDoesntBlockTryPut(DELAY_SMALL_MS);
    }

    /**
     * Check that values that are lend, don't block putting new values
     */
    public void testNotReturnedValueDoesntBlockTryPut(long timeout) {
        Integer oldRef = 10;
        awaitableRef = new DefaultAwaitableReference<Integer>(oldRef);

        tested_take(oldRef);

        Integer newRef = 20;
        put(timeout, newRef, oldRef);

        assertHasReference(newRef);
    }

    @InterruptedFalse
    public void testSuccess_startFromNullValue() {
        testSuccess(null);
    }

    @InterruptedFalse
    public void testSuccess_startFromNonNullValue() {
        testSuccess(new Integer(100));
    }

    public void testSuccess(Integer originalRef) {
        awaitableRef = new DefaultAwaitableReference<Integer>(originalRef);

        Integer newRef = 20;
        put(DELAY_SMALL_MS, newRef, originalRef);
        assertHasReference(newRef);
    }

    private void put(long timeout, Integer newRef, Integer oldRef) {
        TimedTryPutThread putter = scheduleTryPut(newRef, timeout);
        joinAll(putter);
        putter.assertSuccess(oldRef);
    }
}
