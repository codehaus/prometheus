/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

/**
 * The Take_Test unittests the {@link org.codehaus.prometheus.references.DefaultAwaitableReference#take()} method.
 *
 * @author Peter Veentjer.
 */
public class DefaultAwaitableReference_TakeTest extends DefaultAwaitableReference_AbstractTests{

    public void testWaitingTillEndOfTime() {
        awaitableRef = new DefaultAwaitableReference<Integer>();

        //do a take and make sure that the take is waiting
        TakeThread taker = scheduleTake();
        sleepMs(DELAY_LONG_MS);
        taker.assertIsStarted();
        assertHasReference(null);
    }

    public void testInterruptedWhileWaiting() {
        awaitableRef = new DefaultAwaitableReference<Integer>();

        //do a take and make sure it is waiting
        TakeThread taker = scheduleTake();
        giveOthersAChance();
        taker.assertIsStarted();
        assertHasReference(null);

        //now interrupt the take
        taker.interrupt();

        //make sure that the taker is interrupted
        joinAll(taker);
        taker.assertIsInterruptedByException();
        assertHasReference(null);
    }

    //================== no waiting needed ============================================

    public void testNoWaitingNeeded_startInterrupted() throws InterruptedException {
        testNoWaitingNeeded(START_INTERRUPTED);
    }

    public void testNoWaitingNeeded_startUninterrupted() throws InterruptedException {
        testNoWaitingNeeded(START_UNINTERRUPTED);
    }

    public void testNoWaitingNeeded(boolean startInterrupted) throws InterruptedException {
        Integer ref = 10;
        awaitableRef = new DefaultAwaitableReference<Integer>(ref);

        //do a take and make sure it completes
        TakeThread taker = scheduleTake(startInterrupted);
        joinAll(taker);
        taker.assertSuccess(ref);
        taker.assertIsTerminatedWithInterruptStatus(startInterrupted);
        assertHasReference(ref);
    }

    //============== some waiting needed ==============================================

    public void testSomeWaitingNeeded_startInterrupted() throws InterruptedException {
        Integer ref = null;
        awaitableRef = new DefaultAwaitableReference<Integer>(ref);

        //do a take and make sure it was interrupted
        TakeThread takeThread = scheduleTake(START_INTERRUPTED);
        joinAll(takeThread);
        takeThread.assertIsInterruptedByException();
        assertHasReference(ref);
    }

    public void testSomeWaitingNeeded_startUninterrupted() throws InterruptedException {
        awaitableRef = new DefaultAwaitableReference<Integer>();

        Integer newRef = 10;
        TakeThread takeThread1 = scheduleTake(START_UNINTERRUPTED);
        TakeThread takeThread2 = scheduleTake(START_UNINTERRUPTED);

        //check that the takers are waiting
        giveOthersAChance();
        takeThread1.assertIsStarted();
        takeThread2.assertIsStarted();

        //do a put and
        PutThread putThread = schedulePut(newRef);
        tested_put(newRef,null);

        joinAll(putThread, takeThread1, takeThread2);
        takeThread1.assertSuccess(newRef);
        takeThread2.assertSuccess(newRef);
        assertHasReference(newRef);
    }

    //============== spurious wakeups ==================================================

    public void testSpuriousWakeup() throws InterruptedException {
        awaitableRef = new DefaultAwaitableReference<Integer>();

        Integer newRef = 10;
        TakeThread takeThread1 = scheduleTake();
        TakeThread takeThread2 = scheduleTake();

        //check that the takers are waiting
        giveOthersAChance();
        takeThread1.assertIsStarted();
        takeThread2.assertIsStarted();

        Thread spurious = scheduleSpuriousWakeup();
        joinAll(spurious);

        //check that the takers still are waiting
        giveOthersAChance();
        takeThread1.assertIsStarted();
        takeThread2.assertIsStarted();

        //put an item, and make sure that the takers have completed.
        PutThread putter = schedulePut(newRef);
        joinAll(putter, takeThread1, takeThread2);
        takeThread1.assertSuccess(newRef);
        takeThread2.assertSuccess(newRef);
    }
}


