/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;

/**
 * Unittests the {@link DefaultAwaitableReference#put(Object)} method.
 *
 * @author Peter Veentjer.
 */
public class DefaultAwaitableReference_PutTest extends DefaultAwaitableReference_AbstractTest {

    //========= no takers =============

    public void testNoTakers_startUninterrupted() {
        testNoTakers(START_UNINTERRUPTED);
    }

    public void testNoTakers_startInterrupted() {
        testNoTakers(START_INTERRUPTED);
    }

    public void testNoTakers(boolean startInterrupted) {
        Integer oldRef = 10;
        awaitableRef = new DefaultAwaitableReference<Integer>(oldRef);

        //do a put and make sure it completes 
        Integer newRef = 20;
        spawned_put(startInterrupted, oldRef, newRef);
    }

    //========= waiting takers =============

    public void testWaitingTakers() throws InterruptedException {
        Integer oldRef = null;
        awaitableRef = new DefaultAwaitableReference<Integer>(oldRef);

        //do takes and make sure that they are blocking
        TakeThread taker1 = scheduleTake();
        TakeThread taker2 = scheduleTake();
        giveOthersAChance();
        taker1.assertIsStarted();
        taker2.assertIsStarted();

        //first put a null value and make sure the takers are still waiting
        spawned_put(null, oldRef);
        taker1.assertIsStarted();
        taker2.assertIsStarted();
        assertHasReference(null);

        //now enter a non null value and make sure that the takers have completed
        Integer secondNewRef = 2;
        spawned_put(secondNewRef, null);
        joinAll(taker1, taker2);
        assertHasReference(secondNewRef);
        taker1.assertSuccess(secondNewRef);
        taker2.assertSuccess(secondNewRef);
    }

    //========= has takers =============
    // a put should not be blocked by previous takes

    public void testActiveTakers_startUninterrupted() {
        testActiveTakers(START_UNINTERRUPTED);
    }

    public void testActiveTakers_startInterrupted() {
        testActiveTakers(START_INTERRUPTED);
    }

    public void testActiveTakers(boolean startInterrupted) {
        Integer oldRef = 10;
        awaitableRef = new DefaultAwaitableReference<Integer>(oldRef);

        //do a take
        spawned_take(oldRef);

        //now do a put and make sure it completes (a relaxed lendable
        //reference doesn't care about previous takes)
        Integer newRef = 20;
        spawned_put(startInterrupted, oldRef, newRef);
    }
}
