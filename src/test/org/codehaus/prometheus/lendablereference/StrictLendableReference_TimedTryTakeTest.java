/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.lendablereference;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unittests the {@link StrictLendableReference#tryTake(long, TimeUnit)}
 * method. 
 *
 * @author Peter Veentjer.
 */
public class StrictLendableReference_TimedTryTakeTest extends StrictLendableReference_AbstractTest<Integer> {

    public void testArgments() throws TimeoutException, InterruptedException {
        try {
            new StrictLendableReference().tryTake(10, null);
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
    }

    public void testNoWaitingNeeded() throws TimeoutException, InterruptedException {
        Integer ref = 10;
        lendableRef = new StrictLendableReference<Integer>(ref);

        TimedTryTakeThread tryTakeThread = scheduleTimedTryTake(0);

        //make sure that the take was successful.
        joinAll(tryTakeThread);
        tryTakeThread.assertSuccess(ref);        
        assertHasRef(ref);
        assertLendCount(1);
    }

    public void testSomeWaitingNeeded() throws TimeoutException, InterruptedException {
        lendableRef = new StrictLendableReference<Integer>();

        TimedTryTakeThread tryTakeThread = scheduleTimedTryTake(DELAY_LONG_MS);

        //make sure that the tryTake is waiting.
        sleepMs(DELAY_TINY_MS);
        tryTakeThread.assertIsStarted();
        assertLendCount(0);

        //do a put and check that the tryTake was succesfull.
        Integer ref = 10;                
        Thread putThread = schedulePut(ref);
        joinAll(putThread,tryTakeThread);
        tryTakeThread.assertSuccess(ref);        
        assertHasRef(ref);
        assertLendCount(1);
    }

    public void testTooMuchWaiting() throws InterruptedException {
        lendableRef = new StrictLendableReference<Integer>();

        TimedTryTakeThread tryTakeThread = scheduleTimedTryTake(DELAY_SMALL_MS);

        //check that the tryTake is waiting
        sleepMs(DELAY_TINY_MS);
        tryTakeThread.assertIsStarted();

        //wait untill the thread completed and check that it received a timeout.
        joinAll(tryTakeThread);
        tryTakeThread.assertIsTimedOut();
        assertHasRef(null);
        assertLendCount(0);
    }

    public void testInterruptedWhileWaiting() throws TimeoutException {
        lendableRef = new StrictLendableReference<Integer>();

        TimedTryTakeThread tryTakeThread = scheduleTimedTryTake(DELAY_LONG_MS);

        sleepMs(DELAY_TINY_MS);
        tryTakeThread.assertIsStarted();

        //now interrupt the trytake and check that the
        tryTakeThread.interrupt();
        joinAll(tryTakeThread);
        assertHasRef(null);
        assertLendCount(0);
        tryTakeThread.assertIsInterruptedByException();
    }

    public void testSpuriousWakeup() throws TimeoutException, InterruptedException {
        lendableRef = new StrictLendableReference<Integer>();
        TimedTryTakeThread tryTakeThread = scheduleTimedTryTake(DELAY_LONG_MS);

        //make sure that the tryTake is waiting.
        sleepMs(DELAY_TINY_MS);
        tryTakeThread.assertIsStarted();
        assertLendCount(0);

        Thread spuriousThread = scheduleSpuriousWakeups();
        joinAll(spuriousThread);

        //make sure that the tryTake is waiting.
        sleepMs(DELAY_TINY_MS);
        tryTakeThread.assertIsStarted();
        assertLendCount(0);

        //do a put and check that the tryTake was succesfull.
        Integer newRef = 10;                        
        Thread putThread = schedulePut(newRef);
        joinAll(putThread,tryTakeThread);
        tryTakeThread.assertSuccess(newRef);
        assertHasRef(newRef);
        assertLendCount(1);
    }
}
