/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.awaitablereference;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unittests the {@link DefaultAwaitableReference#tryPut(Object,long,TimeUnit)}
 *
 * @author Peter Veentjer.
 */
public class DefaultAwaitableReference_TimedTryPutTest extends DefaultAwaitableReference_AbstractTests{

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
        PutThread putThread = schedulePut(newRef,START_INTERRUPTED);

        joinAll(putThread);
        putThread.assertSuccess(oldRef);
        assertHasReference(newRef);
    }

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
        TakeThread taker = scheduleTake();
        joinAll(taker);

        Integer newRef = 20;
        TimedTryPutThread putter = scheduleTryPut(newRef, timeout);
        joinAll(putter);

        putter.assertSuccess(oldRef);
        assertHasReference(newRef);
    }

    public void testSuccess_startFromNullValue() {
        testSuccess(null);
    }

    public void testSuccess_startFromNonNullValue() {
        testSuccess(new Integer(100));
    }

    public void testSuccess(Integer originalRef) {
        awaitableRef = new DefaultAwaitableReference<Integer>(originalRef);

        Integer newRef = 20;
        TimedTryPutThread putter = scheduleTryPut(newRef, DELAY_SMALL_MS);

        joinAll(putter);
        putter.assertSuccess(originalRef);
        assertHasReference(newRef);
    }
}
