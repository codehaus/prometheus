/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.awaitablereference;

/**
 * The Take_Test unittests the {@link DefaultAwaitableReference#take()} method.
 *
 * @author Peter Veentjer.
 */
public class DefaultAwaitableReference_TakeTest extends DefaultAwaitableReference_AbstractTests{

    //this test also tests the interrupt functionality, so this doesn't need
    //to be tested anymore.
    public void testWaitingTillEndOfTime() {
        awaitableRef = new DefaultAwaitableReference<Integer>();

        TakeThread taker = scheduleTake();
        sleepMs(DELAY_SMALL_MS);

        taker.assertIsStarted();
        assertHasReference(null);
    }

    public void testInterruptedWhileWaiting() {
        awaitableRef = new DefaultAwaitableReference<Integer>();
        TakeThread taker = scheduleTake();

        sleepMs(DELAY_SMALL_MS);
        taker.assertIsStarted();
        assertHasReference(null);

        taker.interrupt();
        joinAll(taker);
        taker.assertIsInterruptedByException();
        assertHasReference(null);
    }

    //================== no waiting needed ============================================

    public void testNoWaitingNeeded_startInterrupted() throws InterruptedException {
        testNoWaitingNeeded(true);
    }

    public void testNoWaitingNeeded_startUninterrupted() throws InterruptedException {
        testNoWaitingNeeded(false);
    }

    public void testNoWaitingNeeded(boolean startInterrupted) throws InterruptedException {
        Integer ref = 10;
        awaitableRef = new DefaultAwaitableReference<Integer>(ref);

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

        TakeThread takeThread = scheduleTake(true);
        joinAll(takeThread);
        takeThread.assertIsInterruptedByException();
        assertHasReference(ref);
    }

    public void testSomeWaitingNeeded_startUninterrupted() throws InterruptedException {
        awaitableRef = new DefaultAwaitableReference<Integer>();

        Integer newRef = 10;
        TakeThread takeThread1 = scheduleTake(false);
        TakeThread takeThread2 = scheduleTake(false);
        sleepMs(DELAY_SMALL_MS);
        takeThread1.assertIsStarted();
        takeThread2.assertIsStarted();

        PutThread putThread = schedulePut(newRef);
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
        sleepMs(DELAY_SMALL_MS);
        takeThread1.assertIsStarted();
        takeThread2.assertIsStarted();


        Thread spurious = scheduleSpuriousWakeup();
        joinAllAndSleepMs(DELAY_SMALL_MS, spurious);
        takeThread1.assertIsStarted();
        takeThread2.assertIsStarted();

        PutThread putter = schedulePut(newRef);
        joinAll(putter, takeThread1, takeThread2);
        takeThread1.assertSuccess(newRef);
        takeThread2.assertSuccess(newRef);
    }
}


