/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import org.codehaus.prometheus.testsupport.TestThread;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Unittests the {@link StrictLendableReference#take()} method.
 *
 * @author Peter Veentjer.
 */
public class StrictLendableReference_TakeTest extends StrictLendableReference_AbstractTest<Integer> {

    public void testNoWaitingNeeded() throws InterruptedException {
        Integer originalRef = 10;
        lendableRef = new StrictLendableReference<Integer>(originalRef);

        //do a take and make sure it completes
        TakeThread<Integer> takeThread = scheduleTake();
        joinAll(takeThread);
        takeThread.assertSuccess(originalRef);
        assertHasRef(originalRef);
        assertLendCount(1);
        assertPutWaits(originalRef + 1);
    }

    public void testSomeWaitingNeeded() throws InterruptedException {
        lendableRef = new StrictLendableReference<Integer>();

        //check that the takes are waiting
        TakeThread<Integer> takeThread1 = scheduleTake();
        TakeThread<Integer> takeThread2 = scheduleTake();
        giveOthersAChance();
        assertLendCount(0);
        takeThread1.assertIsStarted();
        takeThread2.assertIsStarted();

        //place a new value, and check that the takes were successful.
        Integer ref = 10;
        tested_put(ref, null);
        joinAll(takeThread1, takeThread2);
        takeThread1.assertSuccess(ref);
        takeThread2.assertSuccess(ref);
        assertHasRef(ref);
        assertLendCount(2);
        assertPutWaits(ref + 1);
    }

    public void testStartWithInterruptedStatus() {
        Integer ref = 10;
        lendableRef = new StrictLendableReference<Integer>(ref);

        //do a take with interruptstatus, and check that the call is interrupted
        TakeThread<Integer> takeThread1 = scheduleTake(START_INTERRUPTED);
        joinAll(takeThread1);
        takeThread1.assertIsInterruptedByException();
        assertLendCount(0);
        assertHasRef(ref);
    }

    public void testInterruptedWhileWaiting() {
        lendableRef = new StrictLendableReference<Integer>();

        //do a take, and check that it is waiting
        TakeThread takeThread = scheduleTake();
        giveOthersAChance();
        takeThread.assertIsStarted();

        //interrupt the take
        takeThread.interrupt();
        joinAll(takeThread);
        takeThread.assertIsInterruptedByException();
        assertHasRef(null);
        assertPutIsPossible(1,null);
        assertLendCount(0);
    }

    private void assertPutIsPossible(Integer newRef, Integer oldRef) {
        TimedTryPutThread timedTryPutThread = scheduleTimedTryPut(newRef,0);
        joinAll(timedTryPutThread);
        timedTryPutThread.assertSuccess(oldRef);
    }

    public void testWaitingTillEndOfTime() {
        lendableRef = new StrictLendableReference<Integer>();
        TakeThread taker = scheduleTake();
        giveOthersAChance();
        taker.assertIsStarted();

        assertHasRef(null);
        assertLendCount(0);
    }

    public void testMultipleTakesFromDifferentThreads() throws InterruptedException {
        Integer ref = 10;
        lendableRef = new StrictLendableReference<Integer>(ref);

        //take it the first time
        tested_take(ref);
        assertLendCount(1);
        assertHasRef(ref);

        //take it the second time
        tested_take(ref);
        assertLendCount(2);
        assertHasRef(ref);

        //take it the third time
        tested_take(ref);
        assertLendCount(3);
        assertHasRef(ref);
    }

    public void testMultipleTakesFromSingleThread() {
        final Integer ref = 10;
        lendableRef = new StrictLendableReference<Integer>(ref);

        int takecount = 10;
        MultipleTakeThread multitakeThread = scheduleMultipleTake(takecount);
        joinAll(multitakeThread);
        multitakeThread.assertSuccess(ref);
        assertLendCount(takecount);
        assertHasRef(ref);
    }

    public MultipleTakeThread scheduleMultipleTake(int count){
        MultipleTakeThread t = new MultipleTakeThread(count);
        t.start();
        return t;
    }

    class MultipleTakeThread extends TestThread {
        private final List<Integer> refList = new LinkedList<Integer>();
        private final int count;

        public MultipleTakeThread(int count) {
            this.count = count;
        }

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            for (int k = 0; k < count; k++)
                refList.add(lendableRef.take());
        }

        public void assertSuccess(Integer expectedRef) {
            assertIsTerminatedWithoutThrowing();
            assertEquals(count,refList.size());
            for(Integer ref: refList){
                assertSame(expectedRef,ref);
            }
        }
    }
    
    //spurious needs to be done from thread that has a readlock
    public void testSpuriousWakeup() throws InterruptedException {
        lendableRef = new StrictLendableReference<Integer>();

        //do takes and make sure they are blocking (because no value is available)
        TakeThread<Integer> takeThread1 = scheduleTake();
        TakeThread<Integer> takeThread2 = scheduleTake();
        giveOthersAChance();
        takeThread1.assertIsStarted();
        takeThread2.assertIsStarted();

        //do a spurious wakeup and see that nothing has changed
        TestThread spuriousThread = scheduleSpuriousWakeups();
        joinAll(spuriousThread);
        spuriousThread.assertIsTerminatedWithoutThrowing();

        giveOthersAChance();
        takeThread1.assertIsStarted();
        takeThread2.assertIsStarted();
        assertHasRef(null);
        assertLendCount(0);

        //check if the takers are notified if a new value is placed.
        Integer newRef = 20;
        Thread putThread = schedulePut(newRef);
        joinAll(putThread, takeThread1, takeThread2);
        takeThread1.assertSuccess(newRef);
        takeThread2.assertSuccess(newRef);
        assertHasRef(newRef);
        assertLendCount(2);
    }
}

