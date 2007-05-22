/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import org.codehaus.prometheus.testsupport.TestThread;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unittests the {@link StrictLendableReference#tryTake(long,TimeUnit)}
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

        //there is a value available, so a take with a 0 timeout should succeed.              
        TimedTryTakeThread tryTakeThread = scheduleTimedTryTake(0);
        joinAll(tryTakeThread);
        tryTakeThread.assertSuccess(ref);
        assertHasRef(ref);
        assertLendCount(1);
    }

    public void testSomeWaitingNeeded() throws TimeoutException, InterruptedException {
        lendableRef = new StrictLendableReference<Integer>();

        //do a timed take, it should block because no value is available
        TimedTryTakeThread tryTakeThread = scheduleTimedTryTake(DELAY_LONG_MS);
        giveOthersAChance();
        tryTakeThread.assertIsStarted();
        assertLendCount(0);

        //do a put and check that the tryTake was succesfull.
        Integer ref = 10;
        tested_put(ref,null);
        joinAll(tryTakeThread);
        tryTakeThread.assertSuccess(ref);
        assertHasRef(ref);
        assertLendCount(1);
    }

    public void testTooMuchWaiting() throws InterruptedException {
        lendableRef = new StrictLendableReference<Integer>();

        //do a timed take, it should block because no value is available
        TimedTryTakeThread tryTakeThread = scheduleTimedTryTake(DELAY_SMALL_MS);
        giveOthersAChance();
        tryTakeThread.assertIsStarted();
        assertLendCount(0);

        //wait untill the thread completed and check that it received a timeout.
        joinAll(tryTakeThread);
        tryTakeThread.assertIsTimedOut();
        assertHasRef(null);
        assertLendCount(0);
    }

    public void testInterruptedWhileWaiting() throws TimeoutException {
        lendableRef = new StrictLendableReference<Integer>();

        //do a timed taked an make sure the call is waiting (because no value can be taken)
        TimedTryTakeThread tryTakeThread = scheduleTimedTryTake(DELAY_LONG_MS);
        giveOthersAChance();
        tryTakeThread.assertIsStarted();
        assertLendCount(0);

        //now interrupt the trytake and check that the call was interrupted
        tryTakeThread.interrupt();
        joinAll(tryTakeThread);
        tryTakeThread.assertIsInterruptedByException();
        assertHasRef(null);
        assertLendCount(0);
    }

    public void testSpuriousWakeup() throws TimeoutException, InterruptedException {
        lendableRef = new StrictLendableReference<Integer>();

        //do a timed take and make sure that it blocks (because no value can be taken)
        TimedTryTakeThread tryTakeThread = scheduleTimedTryTake(DELAY_LONG_MS);
        giveOthersAChance();
        tryTakeThread.assertIsStarted();
        assertLendCount(0);

        //do spurious wakeup
        TestThread spuriousThread = scheduleSpuriousWakeups();
        joinAll(spuriousThread);
        spuriousThread.assertIsTerminatedNormally();

        //make sure that the tryTake is waiting.
        giveOthersAChance();
        tryTakeThread.assertIsStarted();
        assertLendCount(0);

        //do a put and check that the tryTake was succesfull.
        Integer newRef = 10;
        tested_put(newRef,null);
        joinAll(tryTakeThread);
        tryTakeThread.assertSuccess(newRef);
        assertHasRef(newRef);
        assertLendCount(1);
    }
}
