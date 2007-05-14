/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

/**
 * Unittests {@link RelaxedLendableReference#takeback(Object)}.
 *  
 * @author Peter Veentjer
 */
public class RelaxedLendableReference_TakebackTest extends RelaxedLendableReference_AbstractTest<Integer> {

    public void testTakeBackNull() {
        try {
            new RelaxedLendableReference().takeback(null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testTooManyTakebacksShouldBeIgnored() throws InterruptedException {
        Integer ref = 10;
        lendableRef = new RelaxedLendableReference<Integer>(ref);

        TakeThread<Integer> takeThread = scheduleTake();
        joinAll(takeThread);
        takeThread.assertSuccess(ref);

        TakeBackThread<Integer> takebackThread1 = scheduleTakeBack(ref);
        joinAll(takebackThread1);
        takebackThread1.assertSuccess();
        assertHasRef(ref);

        TakeBackThread<Integer> takebackThread2 = scheduleTakeBack(ref);
        joinAll(takebackThread2);
        takebackThread2.assertSuccess();
        assertHasRef(ref);
    }

    //an incorrect reference can be taken back without problems.
    public void testTakebackIncorrectRefShouldBeIgnored_withTake() {
        Integer ref = 10;
        lendableRef = new RelaxedLendableReference<Integer>(ref);

        //take a value
        TakeThread<Integer> takeThread = scheduleTake();
        joinAll(takeThread);
        takeThread.assertSuccess(ref);

        //take back a bogus value
        Integer bogusRef = 20;
        TakeBackThread<Integer> takebackThread1 = scheduleTakeBack(bogusRef);
        joinAll(takebackThread1);
        takebackThread1.assertSuccess();
        assertHasRef(ref);
    }

    public void testTakebackIncorrectRefShouldBeIgnored_withoutTakes() {
        Integer ref = 10;
        lendableRef = new RelaxedLendableReference<Integer>(ref);

        //take back a bogus value
        Integer bogusRef = 20;
        TakeBackThread<Integer> takebackThread1 = scheduleTakeBack(bogusRef);
        joinAll(takebackThread1);
        takebackThread1.assertSuccess();
        assertHasRef(ref);
    }

    public void testTakebackSuccess() throws InterruptedException {
        Integer ref = 10;
        lendableRef = new RelaxedLendableReference<Integer>(ref);
        //take a value
        TakeThread<Integer> takeThread = scheduleTake();
        joinAll(takeThread);
        takeThread.assertSuccess(ref);

        //take back a bogus value
        TakeBackThread<Integer> takebackThread1 = scheduleTakeBack(ref);
        joinAll(takebackThread1);
        takebackThread1.assertSuccess();
        assertHasRef(ref);
    }
}