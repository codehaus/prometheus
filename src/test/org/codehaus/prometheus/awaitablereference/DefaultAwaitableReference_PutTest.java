/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.awaitablereference;

/**
 * Unittests the {@link DefaultAwaitableReference#put(Object)} method.
 *
 * @author Peter Veentjer.
 */
public class DefaultAwaitableReference_PutTest extends DefaultAwaitableReference_AbstractTests {

    public void testStartInterrupted() {
        Integer oldRef = 10;
        awaitableRef = new DefaultAwaitableReference<Integer>(oldRef);

        Integer newRef = 20;
        PutThread putThread = schedulePut(newRef, START_INTERRUPTED);

        joinAll(putThread);
        putThread.assertSuccess(oldRef);
        assertHasReference(newRef);
    }

    public void testPut() throws InterruptedException {
        Integer oldRef = null;
        awaitableRef = new DefaultAwaitableReference<Integer>(oldRef);

        TakeThread taker1 = scheduleTake();
        TakeThread taker2 = scheduleTake();

        //make sure the takes are waiting
        giveOthersAChance();
        taker1.assertIsStarted();
        taker2.assertIsStarted();

        //first enter a null value
        put(null, oldRef);
        taker1.assertIsStarted();
        taker2.assertIsStarted();
        assertHasReference(null);

        //now enter a non null value
        Integer secondNewRef = 2;
        put(secondNewRef, null);
        PutThread putter = schedulePut(secondNewRef);
        joinAll(putter, taker1, taker2);
        assertHasReference(secondNewRef);

        taker1.assertSuccess(secondNewRef);
        taker2.assertSuccess(secondNewRef);
    }

    private void put(Integer newRef, Integer expectedReturnedRef) {
        PutThread putter = schedulePut(newRef);
        joinAll(putter);
        putter.assertSuccess(expectedReturnedRef);
    }
}
