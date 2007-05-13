/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.awaitablereference;

import org.codehaus.prometheus.testsupport.InterruptedTrueFalse;
import org.codehaus.prometheus.testsupport.InterruptedTrue;
import org.codehaus.prometheus.testsupport.InterruptedFalse;
import org.codehaus.prometheus.references.DefaultAwaitableReference;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unittests {@link org.codehaus.prometheus.references.DefaultAwaitableReference#tryTake(long,TimeUnit)} method.
 *
 * @author Peter Veentjer.
 */
public class DefaultAwaitableReference_TimedTryTakeTest extends DefaultAwaitableReference_AbstractTests{

    @InterruptedTrueFalse
    public void testArguments() throws TimeoutException, InterruptedException {
        awaitableRef = new DefaultAwaitableReference<Integer>();

        try {
            awaitableRef.tryTake(10, null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }


    @InterruptedTrueFalse
    public void testNegativeTimeout() throws InterruptedException {
        Integer ref = 10;
        awaitableRef = new DefaultAwaitableReference<Integer>(ref);

        try {
            awaitableRef.tryTake(-1, TimeUnit.NANOSECONDS);
            fail("TimeoutException expected");
        } catch (TimeoutException ex) {
            assertTrue(true);
        }
    }

    //================================================================
    // If there is a value available, no InterruptedException should
    // be thrown.
    //=================================================================

    @InterruptedTrue
    public void testNoWaitingNeeded_startUninterrupted() throws TimeoutException, InterruptedException {
        testNoWaitingNeeded(START_UNINTERRUPTED);
    }

    @InterruptedFalse
    public void testNoWaitingNeeded_startInterrupted() throws TimeoutException, InterruptedException {
        testNoWaitingNeeded(START_INTERRUPTED);
    }

    public void testNoWaitingNeeded(boolean startInterrupted) throws TimeoutException, InterruptedException {
        Integer ref = 10;
        awaitableRef = new DefaultAwaitableReference<Integer>(ref);
        TimedTryTakeThread taker = scheduleTryTake(0,startInterrupted);

        joinAll(taker);
        assertHasReference(ref);
        taker.assertSuccess(ref);
        taker.assertIsTerminatedWithInterruptStatus(startInterrupted);
    }

    //================================================================

    @InterruptedTrue
    public void testSomeWaitingNeeded_startInterrupted(){
        awaitableRef = new DefaultAwaitableReference<Integer>();
        TimedTryTakeThread takeThread = scheduleTryTake(DELAY_SMALL_MS,START_INTERRUPTED);
        joinAll(takeThread);
        takeThread.assertIsInterruptedByException();
    }

    @InterruptedFalse
    public void testSomeWaitingNeeded() throws TimeoutException, InterruptedException {
        Integer ref = 10;
        awaitableRef = new DefaultAwaitableReference<Integer>();
        TimedTryTakeThread takeThread = scheduleTryTake(DELAY_EON_MS);

        giveOthersAChance();
        takeThread.assertIsStarted();

        org.codehaus.prometheus.references.PutThread putThread = schedulePut(ref);

        joinAll(putThread,takeThread);
        takeThread.assertSuccess(ref);
        assertHasReference(ref);
    }

    @InterruptedFalse
    public void testTooMuchWaiting() throws InterruptedException {
        awaitableRef = new DefaultAwaitableReference<Integer>();
        TimedTryTakeThread taker = scheduleTryTake(DELAY_SMALL_MS);

        joinAll(taker);
        taker.assertIsTimedOut();
        assertHasReference(null);
    }

    @InterruptedFalse
    public void testInterruptedWhileWaiting() throws TimeoutException {
        awaitableRef = new DefaultAwaitableReference<Integer>();
        TimedTryTakeThread taker = scheduleTryTake(DELAY_EON_MS);

        giveOthersAChance();
        taker.assertIsStarted();

        taker.interrupt();
        joinAll(taker);
        taker.assertIsInterruptedByException();
        assertHasReference(null);        
    }

    @InterruptedFalse
    public void testSpuriousWakeup() throws TimeoutException, InterruptedException {
        awaitableRef = new DefaultAwaitableReference<Integer>();
        TimedTryTakeThread taker = scheduleTryTake(DELAY_EON_MS);

        giveOthersAChance();
        taker.assertIsStarted();

        Thread spurious = scheduleSpuriousWakeup();
        joinAllAndSleepMs(DELAY_TINY_MS,spurious);
        taker.assertIsStarted();

        Integer ref = 20;
        org.codehaus.prometheus.references.PutThread putter = schedulePut(ref);

        joinAll(putter,taker);
        taker.assertSuccess(ref);
        assertHasReference(ref);
    }
}
