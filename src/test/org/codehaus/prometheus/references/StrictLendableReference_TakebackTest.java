/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.joinAll;
import org.codehaus.prometheus.testsupport.Delays;

/**
 * Unittests the {@link StrictLendableReference#takeback(Object)} method.
 *
 * @author Peter Veentjer.
 */
public class StrictLendableReference_TakebackTest extends StrictLendableReference_AbstractTest<Integer> {

    //null values can be returned, so it should fail
    public void testTakeBackNull() throws InterruptedException {
        try {
            new StrictLendableReference().takeback(null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    //a strict reference doesn't accept an incorrect reference to be taken back
    public void testTakeBackIncorrectReference() throws InterruptedException {
        Integer originalRef = 10;
        Integer takebackRef = 20;
        lendableRef = new StrictLendableReference(originalRef);

        LendThread<Integer> lender = scheduleLend(takebackRef, Delays.SMALL_MS);
        joinAll(lender);
        lender.assertIsIncorrectRef();
        assertLendCount(1);
        assertHasRef(originalRef);
        assertPutWaits(30);
    }

    //a lend reference can be returned by a different thread
    public void testTakebackByDifferentThread() {
        Integer originalRef = 10;
        lendableRef = new StrictLendableReference<Integer>(originalRef);

        spawned_take(originalRef);
        spawned_takeback(originalRef);

        assertHasRef(originalRef);
        assertLendCount(0);
        spawned_put(20);
    }


    //a value that is lend multiple times by different threads, can be
    //taken back multiple times by a single thread
    public void testTakeBackBySameThreadMultipleTimes() {
        Integer orignalRef = 10;
        lendableRef = new StrictLendableReference<Integer>(orignalRef);

        TakeThread takeThread1 = scheduleTake();
        TakeThread takeThread2 = scheduleTake();
        TakeThread takeThread3 = scheduleTake();
        joinAll(takeThread1, takeThread2, takeThread3);

        MultipleTakebackThread takebackThread = scheduleMultipleTakebacks(3, orignalRef);
        joinAll(takebackThread);
        takebackThread.assertSuccess();

        assertHasRef(orignalRef);
        assertLendCount(0);
    }

    //a takeback of a non 'illegal reference' is not allowed.
    public void testTakebackWhileNotTaken() {
        Integer originalRef = 10;
        lendableRef = new StrictLendableReference<Integer>(originalRef);

        TakeBackThread takeBack = scheduleTakeback(originalRef);
        joinAll(takeBack);

        takeBack.assertIllegalTakeback();
        assertLendCount(0);
        assertHasRef(originalRef);
        spawned_put(30);
    }

    //take followed by takeback of different instances of the same object
    //(so equals match) is allowed.
    public void testLendByOneThread_equalObject() {
        Integer originalRef = new Integer(10);
        Integer takenbackRef = new Integer(10);
        testLendByOneThread(originalRef, takenbackRef);
    }

    //take followed by takeback of same instance is allowed (ofcourse)
    public void testLendByOneThread_sameObject() {
        Integer originalRef = new Integer(10);
        testLendByOneThread(originalRef, originalRef);
    }

    public void testLendByOneThread(Integer originalRef, Integer takebackRef) {
        lendableRef = new StrictLendableReference<Integer>(originalRef);

        LendThread<Integer> lender = scheduleLend(takebackRef, Delays.MEDIUM_MS);
        giveOthersAChance();
        lender.assertIsTaken(originalRef);

        joinAll(lender);
        lender.assertIsTakenBack(originalRef);
        assertHasRef(originalRef);
        spawned_put(20);
    }
}

