/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.*;
import org.codehaus.prometheus.testsupport.Delays;

/**
 * The Take_Test unittests the {@link org.codehaus.prometheus.references.DefaultAwaitableReference#take()} method.
 *
 * @author Peter Veentjer.
 */
public class DefaultAwaitableReference_TakeTest extends DefaultAwaitableReference_AbstractTest {

    public void testWaitingTillEndOfTime() {
        awaitableRef = new DefaultAwaitableReference<Integer>();

        //do a take and make sure that the take is waiting
        TakeThread taker = scheduleTake();
        sleepMs(Delays.LONG_MS);
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
        taker.assertIsTerminatedByInterruptedException();
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
        taker.assertIsTerminatedWithInterruptFlag(startInterrupted);

        assertHasReference(ref);
    }

    //============== some waiting needed ==============================================

    public void testSomeWaitingNeeded_startInterrupted() throws InterruptedException {
        Integer ref = null;
        awaitableRef = new DefaultAwaitableReference<Integer>(ref);

        //do a take and make sure it was interrupted
        TakeThread takeThread = scheduleTake(START_INTERRUPTED);
        joinAll(takeThread);
        takeThread.assertIsTerminatedByInterruptedException();
        assertHasReference(ref);
    }

    public void testSomeWaitingNeeded_startUninterrupted() throws InterruptedException {
        awaitableRef = new DefaultAwaitableReference<Integer>();

        //do takes and make sure that they are waiting        
        TakeThread takeThread1 = scheduleTake(START_UNINTERRUPTED);
        TakeThread takeThread2 = scheduleTake(START_UNINTERRUPTED);
        giveOthersAChance();
        takeThread1.assertIsStarted();
        takeThread2.assertIsStarted();

        //do a put and check that the takes completed
        Integer newRef = 10;
        spawned_put(newRef, null);
        joinAll(takeThread1, takeThread2);
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

        doSpuriousWakeup();

        //check that the takers still are waiting
        giveOthersAChance();
        takeThread1.assertIsStarted();
        takeThread2.assertIsStarted();

        //put an item, and make sure that the takers have completed.
        spawned_put(newRef, null);
        joinAll(takeThread1, takeThread2);
        takeThread1.assertSuccess(newRef);
        takeThread2.assertSuccess(newRef);
    }
}


