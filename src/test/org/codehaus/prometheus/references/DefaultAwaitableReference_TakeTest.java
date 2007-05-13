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

        TakeThread taker = scheduleTake();

        //make sure that the taker is waiting
        giveOthersAChance();
        taker.assertIsStarted();
        assertHasReference(null);
    }

    public void testInterruptedWhileWaiting() {
        awaitableRef = new DefaultAwaitableReference<Integer>();
        TakeThread taker = scheduleTake();

        //make sure that the taker is waiting
        giveOthersAChance();
        taker.assertIsStarted();
        assertHasReference(null);

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

        TakeThread taker = scheduleTake(startInterrupted);

        //make sure take has completed
        joinAll(taker);
        taker.assertSuccess(ref);
        taker.assertIsTerminatedWithInterruptStatus(startInterrupted);
        assertHasReference(ref);
    }

    //============== some waiting needed ==============================================

    public void testSomeWaitingNeeded_startInterrupted() throws InterruptedException {
        Integer ref = null;
        awaitableRef = new DefaultAwaitableReference<Integer>(ref);

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

        //now place an item
        PutThread putThread = schedulePut(newRef);

        //and check that the takers have completed
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


