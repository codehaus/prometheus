/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import org.codehaus.prometheus.references.StrictLendableReference;
import org.codehaus.prometheus.references.TakeThread;

/**
 * Unittests the {@link StrictLendableReference#put(Object)} method. 
 *
 * @author Peter Veentjer.
 */
public class StrictLendableReference_PutTest extends StrictLendableReference_AbstractTest<Integer> {

    //==========================================================
    // The first test deals with starting interrupted. If the thread
    // has interruptstatus, and that thread executes a put, it
    // receives an InterruptedException.
    //==========================================================

    public void testStartInterrupted(){
        Integer oldRef = 10;
        lendableRef = new StrictLendableReference<Integer>(oldRef);

        Integer newRef = 20;
        PutThread putThread = schedulePut(newRef,START_INTERRUPTED);

        joinAll(putThread);
        putThread.assertIsInterruptedByException();
        assertHasRef(oldRef);
    }

    //==========================================================
    //all tests below start uninterrupted    
    //==========================================================

    public void testPutNull() throws InterruptedException {
        lendableRef = new StrictLendableReference<Integer>();
        TakeThread takeThread1 = scheduleTake();
        TakeThread takeThread2 = scheduleTake();

        //put null and make sure that the takers are still waiting
        giveOthersAChance();
        tested_put(null,null);

        giveOthersAChance();
        takeThread1.assertIsStarted();
        takeThread2.assertIsStarted();

        //putUninterruptibly new value and make sure that the takers have
        //taken the expected value.
        Integer newRef = 1;
        PutThread putThread = schedulePut(newRef);
        joinAll(putThread, takeThread1, takeThread2);
        takeThread1.assertSuccess(newRef);
        takeThread2.assertSuccess(newRef);
        putThread.assertSuccess(null);
        assertHasRef(newRef);
    }
 
    public void testNoWaiting() {
        lendableRef = new StrictLendableReference<Integer>();
        Integer ref = 10;

        PutThread<Integer> putThread = schedulePut(ref);
        joinAll(putThread);
        putThread.assertSuccess(null);

        assertHasRef(ref);
    }

    //a different thread has taken a value, so the put neets to wait untill the value is returned.
    public void testSomeWaitingNeeded(Integer newRef) {
        Integer oldRef = newRef==null?1:newRef + 10;
        lendableRef = new StrictLendableReference<Integer>(oldRef);

        //take a reference so that the put is going to block
        tested_take(oldRef);

        PutThread<Integer> putThread = schedulePut(newRef);
        giveOthersAChance();
        putThread.assertIsStarted();
        assertHasRef(oldRef);

        //return the old reference
        tested_takeback(oldRef);

        //now wait for the completion of the lend and the put
        //and check if the put has taken place
        joinAll(putThread);
        putThread.assertSuccess(oldRef);
        assertHasRef(newRef);
    }

    public void testSomeWaitingNeeded_putNull(){
        testSomeWaitingNeeded(null);
    }

    public void testSomeWaitingNeeded_putNonNull(){
        testSomeWaitingNeeded(10);
    }

    public void testInterruptedWhileWaiting() {
        Integer oldRef = 10;
        Integer newRef = 20;
        lendableRef = new StrictLendableReference<Integer>(oldRef);

        //take a reference so the put is going to block
        tested_take(oldRef);

        PutThread<Integer> putThread = schedulePut(newRef);
        //make sure that the put is still waiting
        giveOthersAChance();
        putThread.assertIsStarted();

        //interrupt the put and make sure that it was interrupted
        putThread.interrupt();

        joinAll(putThread);
        putThread.assertIsInterruptedByException();
        assertHasRef(oldRef);
    }

    public void testSpuriousWakeup() {
        Integer takenref = 10;
        Integer putref = 20;
        lendableRef = new StrictLendableReference<Integer>(takenref);
        tested_take(takenref);

        PutThread<Integer> putThread = schedulePut(putref);
        //make sure that the put is waiting
        giveOthersAChance();
        putThread.assertIsStarted();

        //do a spurious wakeup and make sure that the put is still waiting
        Thread spurious = scheduleSpuriousWakeups();

        joinAll(spurious);
        giveOthersAChance();
        putThread.assertIsStarted();
        assertHasRef(takenref);

        tested_takeback(takenref);
                
        //new let the lend and the put complete.
        joinAll(putThread);
        putThread.assertSuccess(takenref);
        assertHasRef(putref);
    }
}
