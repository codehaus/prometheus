/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unittests the {@link StrictLendableReference#tryPut(Object, long, TimeUnit)}
 * method. 
 *
 * @author Peter Veentjer.
 */
public class StrictLendableReference_TimedTryPut extends StrictLendableReference_AbstractTest<Integer> {

    public void testArguments() throws TimeoutException, InterruptedException {
        try {
            new StrictLendableReference().tryPut(10, 10, null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testNegativeTimeout() throws InterruptedException {
        Integer ref = 10;
        lendableRef = new StrictLendableReference<Integer>(ref);
        try {
            lendableRef.tryTake(-1, TimeUnit.NANOSECONDS);
            fail("TimeoutException expected");
        } catch (TimeoutException e) {
            assertTrue(true);
        }
        assertHasRef(ref);
        assertLendCount(0);
    }

    public void testPutNull() {
        Integer ref = 10;
        lendableRef = new StrictLendableReference<Integer>(ref);

        TimedTryPutThread tryPutThread = scheduleTimedTryPut(null,0);
        joinAll(tryPutThread);

        assertHasRef(null);
        tryPutThread.assertSuccess(ref);
        assertLendCount(0);
    }

    public void testNoWaitingNeeded() {
        lendableRef = new StrictLendableReference<Integer>();
        Integer ref = 10;

        TimedTryPutThread tryPutThread = scheduleTimedTryPut(ref,0);
        joinAll(tryPutThread);

        assertHasRef(ref);
        tryPutThread.assertSuccess(null);
        assertLendCount(0);
    }

    public void testSomeWaitingNeeded() {
        Integer originalRef = 10;
        Integer newRef = 20;
        lendableRef = new StrictLendableReference<Integer>(originalRef);
        TakeThread takeThread = scheduleTake();

        joinAll(takeThread);
        takeThread.assertSuccess(originalRef);

        TimedTryPutThread tryPutThread = scheduleTimedTryPut(newRef, DELAY_LONG_MS);

        sleepMs(DELAY_TINY_MS);
        tryPutThread.assertIsStarted();
        assertHasRef(originalRef);
        assertLendCount(1);

        TakeBackThread takeBackThread = scheduleTakeBack(originalRef);
        joinAll(tryPutThread,takeBackThread);

        tryPutThread.assertSuccess(originalRef);
        assertHasRef(newRef);
        assertLendCount(0);
    }

    public void testTooMuchWaiting() {
        Integer originalRef = 10;
        Integer newRef = 20;
        lendableRef = new StrictLendableReference<Integer>(originalRef);
        TakeThread takeThread = scheduleTake();
        joinAll(takeThread);

        TimedTryPutThread tryPutThread = scheduleTimedTryPut(newRef, DELAY_TINY_MS);
        joinAll(tryPutThread);

        tryPutThread.assertIsTimedOut();
        assertHasRef(originalRef);
        assertLendCount(1);
    }

    public void testInterruptedWhileWaiting() {
        Integer originalRef = 10;
        lendableRef = new StrictLendableReference<Integer>(originalRef);
        Thread takeThread = scheduleTake();
        joinAll(takeThread);

        Integer newRef = 20;
        TimedTryPutThread tryPutThread = scheduleTimedTryPut(newRef, DELAY_LONG_MS);
        sleepMs(DELAY_TINY_MS);

        tryPutThread.assertIsStarted();
        assertHasRef(originalRef);

        tryPutThread.interrupt();
        sleepMs(DELAY_TINY_MS);
        tryPutThread.assertIsInterruptedByException();
        assertHasRef(originalRef);
        assertLendCount(1);
    }

    public void testSpuriousWakeup() {
        Integer originalRef = 10;
        Integer newRef = 20;
        lendableRef = new StrictLendableReference<Integer>(originalRef);
        TakeThread takeThread = scheduleTake();
        joinAll(takeThread);

        //the tryPut is waiting         
        TimedTryPutThread tryPutThread = scheduleTimedTryPut(newRef, DELAY_LONG_MS);
        sleepMs(DELAY_TINY_MS);
        tryPutThread.assertIsStarted();

        //do some spurious wakeups.
        Thread spuriousThread = scheduleSpuriousWakeups();
        joinAll(spuriousThread);

        //make sure it is still waiting
        sleepMs(DELAY_TINY_MS);
        tryPutThread.assertIsStarted();

        //take the item back and make sure that tryPut went ok.
        TakeBackThread takebackThread = scheduleTakeBack(originalRef);
        joinAll(takebackThread, tryPutThread);
        tryPutThread.assertSuccess(originalRef);
        assertHasRef(newRef);
        assertLendCount(0);
    }
}
