/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.lendablereference;

/**
 * Unittests the {@link StrictLendableReference#take()} method.
 *
 * @author Peter Veentjer.
 */
public class StrictLendableReference_TakeTest extends StrictLendableReference_AbstractTest<Integer> {

    public void testNoWaitingNeeded() throws InterruptedException {
        Integer originalRef = 10;
        lendableRef = new StrictLendableReference<Integer>(originalRef);
        TakeThread<Integer> takeThread = scheduleTake();
        joinAll(takeThread);

        takeThread.assertSuccess(originalRef);
        assertHasRef(originalRef);
        assertLendCount(1);
        assertPutWaits(1);
    }

    public void testSomeWaitingNeeded() throws InterruptedException {
        lendableRef = new StrictLendableReference<Integer>();
        TakeThread<Integer> takeThread1 = scheduleTake();
        TakeThread<Integer> takeThread2 = scheduleTake();

        //check that the takes are waiting
        sleepMs(DELAY_SMALL_MS);
        assertLendCount(0);
        takeThread1.assertIsStarted();
        takeThread2.assertIsStarted();

        //place a new value, and check that the takes were successful.
        Integer newRef = 10;
        Thread putThread = schedulePut(newRef);
        joinAll(putThread,takeThread1,takeThread2);
        
        takeThread1.assertSuccess(newRef);
        takeThread2.assertSuccess(newRef);
        assertHasRef(newRef);
        assertLendCount(2);
        assertPutWaits(1);
    }

    public void testStartWithInterruptedStatus(){
        Integer ref = 10;
        lendableRef = new StrictLendableReference<Integer>(ref);
        TakeThread<Integer> takeThread1 = scheduleTake(START_INTERRUPTED);
        joinAll(takeThread1);

        takeThread1.assertInterrupted();
        assertLendCount(0);
        assertHasRef(ref);
    }

    public void testInterruptedWhileWaiting(){
        lendableRef = new StrictLendableReference<Integer>();
        TakeThread takeThread = scheduleTake();

        //make sure that the take is waiting
        sleepMs(DELAY_TINY_MS);
        takeThread.assertIsStarted();

        //interrupt the take
        takeThread.interrupt();
        joinAll(takeThread);

        takeThread.assertInterrupted();
        assertHasRef(null);
        assertPutIsPossible(1);
        assertLendCount(0);
    }

    private void assertPutIsPossible(Integer i) {
        //todo
    }

    public void testWaitingTillEndOfTime() {
        lendableRef = new StrictLendableReference<Integer>();
        TakeThread taker = scheduleTake();

        sleepMs(500);

        taker.assertIsStarted();
        assertHasRef(null);
        assertLendCount(0);

        taker.interruptAndJoin();
        assertPutIsPossible(1);
    }

    public void testMultipleTakesFromSingleThread() throws InterruptedException {
        Integer ref = 10;
        lendableRef = new StrictLendableReference<Integer>(ref);

        Integer ref1 = lendableRef.take();
        assertLendCount(1);
        assertSame(ref,ref1);

        Integer ref2 = lendableRef.take();
        assertLendCount(2);
        assertSame(ref,ref2);

        Integer ref3 = lendableRef.take();
        assertLendCount(3);
        assertSame(ref,ref3);
    }

    //spurious needs to be done from thread that has a readlock
    public void testSpuriousWakeup() throws InterruptedException {
        lendableRef = new StrictLendableReference<Integer>();
        TakeThread<Integer> takeThread1 = scheduleTake();
        TakeThread<Integer> takeThread2 = scheduleTake();

        sleepMs(DELAY_TINY_MS);
        takeThread1.assertIsStarted();
        takeThread2.assertIsStarted();

        Thread spuriousThread = scheduleDelayedSpuriousWakeups();
        joinAll(spuriousThread);

        //check if the takers are still waiting and the ref is not changed
        sleepMs(DELAY_TINY_MS);
        takeThread1.assertIsStarted();
        takeThread2.assertIsStarted();
        assertHasRef(null);
        assertLendCount(0);

        //check if the takers are notified if a new value is placed.
        Integer newRef = 20;
        Thread putThread = schedulePut(newRef);
        joinAll(putThread,takeThread1,takeThread2);
        takeThread1.assertSuccess(newRef);
        takeThread2.assertSuccess(newRef);
        assertHasRef(newRef);
        assertLendCount(2);
    }
}

