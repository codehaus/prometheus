/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import org.codehaus.prometheus.testsupport.TestThread;
import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unittests the {@link StrictLendableReference#tryPut(Object,long,TimeUnit)}
 * method.
 *
 * @author Peter Veentjer.
 */
public class StrictLendableReference_TimedTryPutTest extends StrictLendableReference_AbstractTest<Integer> {

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

        TimedTryPutThread tryPutThread = scheduleTimedTryPut(null, 0);
        joinAll(tryPutThread);

        assertHasRef(null);
        tryPutThread.assertSuccess(ref);
        assertLendCount(0);
    }

    public void testNoWaitingNeeded() {
        lendableRef = new StrictLendableReference<Integer>();
        Integer ref = 10;

        //a put with a nul timeout should complete without problems
        TimedTryPutThread tryPutThread = scheduleTimedTryPut(ref, 0);
        joinAll(tryPutThread);
        assertHasRef(ref);
        tryPutThread.assertSuccess(null);
        assertLendCount(0);
    }

    public void testSomeWaitingNeeded() {
        Integer originalRef = 10;
        Integer newRef = 20;
        lendableRef = new StrictLendableReference<Integer>(originalRef);

        //first execute a take
        tested_take(originalRef);

        //now do a put, it should block because something is taken
        TimedTryPutThread tryPutThread = scheduleTimedTryPut(newRef, DELAY_LONG_MS);
        giveOthersAChance();
        tryPutThread.assertIsStarted();
        assertHasRef(originalRef);
        assertLendCount(1);

        //now bring the item back and check that the put now is able to complete
        TakeBackThread takeBackThread = scheduleTakeback(originalRef);
        joinAll(tryPutThread, takeBackThread);
        tryPutThread.assertSuccess(originalRef);
        assertHasRef(newRef);
        assertLendCount(0);
    }

    public void testTooMuchWaiting() {
        Integer originalRef = 10;
        Integer newRef = 20;
        lendableRef = new StrictLendableReference<Integer>(originalRef);

        //do a take
        tested_take(originalRef);

        //a timed put is going to timeout because a value is taken 
        TimedTryPutThread tryPutThread = scheduleTimedTryPut(newRef, DELAY_TINY_MS);
        joinAll(tryPutThread);
        tryPutThread.assertIsTimedOut();
        assertHasRef(originalRef);
        assertLendCount(1);
    }

    public void testInterruptedWhileWaiting() {
        Integer originalRef = 10;
        lendableRef = new StrictLendableReference<Integer>(originalRef);

        //do a take
        tested_take(originalRef);

        //do a put and make sure that it is waiting because a value is taken
        Integer newRef = 20;
        TimedTryPutThread tryPutThread = scheduleTimedTryPut(newRef, DELAY_LONG_MS);
        giveOthersAChance();
        tryPutThread.assertIsStarted();
        assertHasRef(originalRef);

        //now interrupt the put and make sure that it is interrupted
        tryPutThread.interrupt();
        giveOthersAChance();
        tryPutThread.assertIsTerminatedByInterruptedException();
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
        giveOthersAChance();
        tryPutThread.assertIsStarted();

        //do some spurious wakeups.
        TestThread spuriousThread = scheduleSpuriousWakeups();
        joinAll(spuriousThread);
        spuriousThread.assertIsTerminatedNormally();

        //make sure it is still waiting
        giveOthersAChance();
        tryPutThread.assertIsStarted();

        //take the item back and make sure that tryPut went ok.
        TakeBackThread takebackThread = scheduleTakeback(originalRef);
        joinAll(takebackThread, tryPutThread);
        tryPutThread.assertSuccess(originalRef);
        takebackThread.assertSuccess();
        assertHasRef(newRef);
        assertLendCount(0);
    }
}
