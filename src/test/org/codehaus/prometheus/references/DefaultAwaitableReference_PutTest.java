/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import org.codehaus.prometheus.references.DefaultAwaitableReference;

/**
 * Unittests the {@link org.codehaus.prometheus.references.DefaultAwaitableReference#put(Object)} method.
 *
 * @author Peter Veentjer.
 */
public class DefaultAwaitableReference_PutTest extends DefaultAwaitableReference_AbstractTests {

    //========= no takers =============

    public void testNoTakers_startUninterrupted(){
        testNoTakers(START_UNINTERRUPTED);
    }

    public void testNoTakers_startInterrupted(){
        testNoTakers(START_INTERRUPTED);
    }

    public void testNoTakers(boolean startInterrupted) {
        Integer oldRef = 10;
        awaitableRef = new DefaultAwaitableReference<Integer>(oldRef);

        Integer newRef = 20;
        org.codehaus.prometheus.references.PutThread putThread = schedulePut(newRef, startInterrupted);

        joinAll(putThread);
        putThread.assertSuccess(oldRef);
        putThread.assertIsTerminatedWithInterruptStatus(startInterrupted);
        assertHasReference(newRef);
    }

    //========= waiting takers =============

    public void testWaitingTakers() throws InterruptedException {
        Integer oldRef = null;
        awaitableRef = new DefaultAwaitableReference<Integer>(oldRef);

        org.codehaus.prometheus.references.TakeThread taker1 = scheduleTake();
        org.codehaus.prometheus.references.TakeThread taker2 = scheduleTake();

        //make sure the takes are waiting
        giveOthersAChance();
        taker1.assertIsStarted();
        taker2.assertIsStarted();

        //first put a null value and make sure the takers are still waiting
        put(null, oldRef);
        taker1.assertIsStarted();
        taker2.assertIsStarted();
        assertHasReference(null);

        //now enter a non null value and make sure that the takers have completed
        Integer secondNewRef = 2;
        put(secondNewRef, null);
        joinAll(taker1, taker2);
        assertHasReference(secondNewRef);
        taker1.assertSuccess(secondNewRef);
        taker2.assertSuccess(secondNewRef);
    }

    //========= has takers =============
    // a put should not be blocked by previous takes

    public void testActiveTakers_startUninterrupted(){
        testActiveTakers(START_UNINTERRUPTED);
    }
    
    public void testActiveTakers_startInterrupted(){
        testActiveTakers(START_INTERRUPTED);
    }

    public void testActiveTakers(boolean startInterrupted){
        Integer oldRef = 10;
        awaitableRef = new DefaultAwaitableReference<Integer>(oldRef);

        take(oldRef);

        Integer newRef = 20;
        org.codehaus.prometheus.references.PutThread putThread = schedulePut(newRef,startInterrupted);

        joinAll(putThread);
        putThread.assertSuccess(oldRef);
        putThread.assertIsTerminatedWithInterruptStatus(startInterrupted);
        assertHasReference(newRef);
    }

}
