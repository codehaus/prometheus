/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import org.codehaus.prometheus.references.DefaultAwaitableReference;

/**
 * The Take_Test unittests the {@link org.codehaus.prometheus.references.DefaultAwaitableReference#take()} method.
 *
 * @author Peter Veentjer.
 */
public class DefaultAwaitableReference_TakeTest extends DefaultAwaitableReference_AbstractTests{

    public void testWaitingTillEndOfTime() {
        awaitableRef = new DefaultAwaitableReference<Integer>();

        org.codehaus.prometheus.references.TakeThread taker = scheduleTake();

        //make sure that the taker is waiting
        giveOthersAChance();
        taker.assertIsStarted();
        assertHasReference(null);
    }

    public void testInterruptedWhileWaiting() {
        awaitableRef = new DefaultAwaitableReference<Integer>();
        org.codehaus.prometheus.references.TakeThread taker = scheduleTake();

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

        org.codehaus.prometheus.references.TakeThread taker = scheduleTake(startInterrupted);

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

        org.codehaus.prometheus.references.TakeThread takeThread = scheduleTake(START_INTERRUPTED);

        joinAll(takeThread);
        takeThread.assertIsInterruptedByException();
        assertHasReference(ref);
    }

    public void testSomeWaitingNeeded_startUninterrupted() throws InterruptedException {
        awaitableRef = new DefaultAwaitableReference<Integer>();

        Integer newRef = 10;
        org.codehaus.prometheus.references.TakeThread takeThread1 = scheduleTake(START_UNINTERRUPTED);
        org.codehaus.prometheus.references.TakeThread takeThread2 = scheduleTake(START_UNINTERRUPTED);

        //check that the takers are waiting
        giveOthersAChance();
        takeThread1.assertIsStarted();
        takeThread2.assertIsStarted();

        //now place an item
        org.codehaus.prometheus.references.PutThread putThread = schedulePut(newRef);

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
        org.codehaus.prometheus.references.TakeThread takeThread1 = scheduleTake();
        org.codehaus.prometheus.references.TakeThread takeThread2 = scheduleTake();

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
        org.codehaus.prometheus.references.PutThread putter = schedulePut(newRef);
        joinAll(putter, takeThread1, takeThread2);
        takeThread1.assertSuccess(newRef);
        takeThread2.assertSuccess(newRef);
    }
}


