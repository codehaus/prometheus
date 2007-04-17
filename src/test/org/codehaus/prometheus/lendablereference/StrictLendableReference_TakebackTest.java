/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.lendablereference;

/**
 * Unittests the {@link StrictLendableReference#takeback(Object)} method.
 *
 * @author Peter Veentjer.
 */
public class StrictLendableReference_TakebackTest extends StrictLendableReference_AbstractTest<Integer> {

    public void testTakeBackNull() throws InterruptedException {
        try {
            new StrictLendableReference().takeback(null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testTakeBackIncorrectReference() throws InterruptedException {
        Integer originalRef = 10;
        Integer takebackRef = 20;
        lendableRef = new StrictLendableReference(originalRef);

        LendThread<Integer> lender = scheduleLend(takebackRef, DELAY_SMALL_MS);
        joinAll(lender);

        lender.assertIsIncorrectRef();
        assertLendCount(1);
        assertHasRef(originalRef);
        assertPutWaits(30);
    }

    public void testTakebackByDifferentThread() {
        Integer originalRef = 10;
        lendableRef = new StrictLendableReference<Integer>(originalRef);

        TakeThread taker = scheduleTake();
        joinAll(taker);

        TakeBackThread takeBack = scheduleTakeBack(originalRef);
        joinAll(takeBack);

        takeBack.assertSuccess();
        assertHasRef(originalRef);
        assertLendCount(0);
        assertPutSucceeds(20);
    }

    public void testTakeBackBySameThreadMultipleTimes() {
        Integer orignalRef = 10;
        lendableRef = new StrictLendableReference<Integer>(orignalRef);

        TakeThread takeThread1 = scheduleTake();
        TakeThread takeThread2 = scheduleTake();
        TakeThread takeThread3 = scheduleTake();
        joinAll(takeThread1,takeThread2,takeThread3);

        MultipleTakeBackThread takebackThread = scheduleMultipleTakebacks(3,orignalRef);
        joinAll(takebackThread);

        takebackThread.assertSuccess();
        assertHasRef(orignalRef);
        assertLendCount(0);
    }

    public void testTakebackWhileNotTaken() {
        Integer originalRef = 10;
        lendableRef = new StrictLendableReference<Integer>(originalRef);

        TakeBackThread takeBack = scheduleTakeBack(originalRef);
        joinAll(takeBack);

        takeBack.assertTakeBackException();
        assertLendCount(0);
        assertHasRef(originalRef);
        assertPutSucceeds(30);
    }

    public void testTakeBackDifferentInstanceEqualValue() {
        Integer originalRef = new Integer(10);
        Integer takenbackRef = new Integer(10);
        testTakeBackBySameThread(originalRef,takenbackRef);
    }

    public void testTakeBackSameInstance() {
        Integer originalRef = new Integer(10);
        testTakeBackBySameThread(originalRef,originalRef);
    }

    public void testTakeBackBySameThread(Integer originalRef, Integer takebackRef) {
        lendableRef = new StrictLendableReference<Integer>(originalRef);

        LendThread<Integer> lender = scheduleLend( takebackRef, DELAY_MEDIUM_MS);
        sleepMs(DELAY_TINY_MS);
        lender.assertIsTaken(originalRef);

        joinAll(lender);
        lender.assertIsTakenBack(originalRef);
        assertHasRef(originalRef);
        assertPutSucceeds(20);
    }
}

