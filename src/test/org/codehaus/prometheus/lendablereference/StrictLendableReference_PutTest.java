/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.lendablereference;

/**
 * Unittests the {@link StrictLendableReference#put(Object)} method. 
 *
 * @author Peter Veentjer.
 */
public class StrictLendableReference_PutTest extends StrictLendableReference_AbstractTest<Integer> {

    public void testStartInterrupted(){
        Integer oldRef = 10;
        lendableRef = new StrictLendableReference<Integer>(oldRef);

        Integer newRef = 20;
        PutThread putThread = schedulePut(newRef,START_INTERRUPTED);
        joinAll(putThread);

        putThread.assertIsInterruptedByException();
        assertHasRef(oldRef);
    }

    public void testPutNull() throws InterruptedException {
        lendableRef = new StrictLendableReference<Integer>();
        TakeThread takeThread1 = scheduleTake();
        TakeThread takeThread2 = scheduleTake();

        //putUninterruptibly null and make sure that the takers are still waiting
        PutThread putThread = schedulePut(null);
        joinAll(putThread);
        sleepMs(DELAY_TINY_MS);
        takeThread1.assertIsStarted();
        takeThread2.assertIsStarted();

        //putUninterruptibly new value and make sure that the takers have
        //taken the expected value.
        Integer newRef = 1;
        putThread = schedulePut(newRef);
        joinAll(putThread, takeThread1, takeThread2);

        takeThread1.assertSuccess(newRef);
        takeThread2.assertSuccess(newRef);
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
    public void testSomeWaitingNeeded() {
        Integer oldRef = 10;
        Integer newRef = 20;
        lendableRef = new StrictLendableReference<Integer>(oldRef);
        LendThread<Integer> lendThread = scheduleLend(oldRef, DELAY_MEDIUM_MS);

        sleepMs(DELAY_TINY_MS);
        PutThread<Integer> putThread = schedulePut(newRef);
        
        //make sure that the put is waiting while the value is not returned.
        sleepMs(DELAY_TINY_MS);
        putThread.assertIsStarted();

        //now wait for the completion of the lend and the put
        //and check if the put has taken place
        joinAll(lendThread, putThread);
        putThread.assertSuccess(oldRef);
        assertHasRef(newRef);
    }

    public void testInterruptedWhileWaiting() {
        Integer oldRef = 10;
        Integer newRef = 20;
        lendableRef = new StrictLendableReference<Integer>(oldRef);
        TakeThread<Integer> takeThread = scheduleTake();
        joinAll(takeThread);
        PutThread<Integer> putThread = schedulePut(newRef);

        //make sure that the put is still waiting
        sleepMs(DELAY_TINY_MS);
        putThread.assertIsStarted();

        //interrupt the put and make sure that it was interrupted
        putThread.interrupt();
        joinAll(putThread);
        putThread.isInterrupted();
        assertHasRef(oldRef);
    }

    public void testSpuriousWakeup() {
        Integer ref = 10;
        Integer newRef = 20;
        lendableRef = new StrictLendableReference<Integer>(ref);
        Thread lendThread = scheduleLend(ref,2* DELAY_SMALL_MS);
        PutThread<Integer> putThread = schedulePut(newRef);

        //make sure that the put is waiting
        sleepMs(DELAY_TINY_MS);
        putThread.assertIsStarted();

        //do a spurious wakeup and make sure that the put is still waiting
        Thread spurious = scheduleSpuriousWakeups();
        joinAllAndSleepMs(DELAY_TINY_MS, spurious);
        putThread.assertIsStarted();
        assertHasRef(ref);

        //new let the lend and the put complete.
        joinAll(lendThread, putThread);
        putThread.assertSuccess(ref);
        assertHasRef(newRef);
    }
}
