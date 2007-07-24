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

    //a null can't be taken back
    public void testTakebackNull() {
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

        //first take a ref
        spawned_take(ref);
        //now take it back
        spawned_takeback(ref);
        assertHasRef(ref);
        //now take it back a second time. This cal should be ignored.
        spawned_takeback(ref);
        assertHasRef(ref);
    }

    //an incorrect reference can be taken back without problems.
    public void testTakebackIncorrectRefShouldBeIgnored_withTake() {
        Integer ref = 10;
        lendableRef = new RelaxedLendableReference<Integer>(ref);

        //take a reference
        spawned_take(ref);
        //now bring back a bogus reference
        Integer bogusRef = 20;
        spawned_takeback(bogusRef);
        assertHasRef(ref);
    }

    public void testTakebackIncorrectRefShouldBeIgnored_withoutTakes() {
        Integer ref = 10;
        lendableRef = new RelaxedLendableReference<Integer>(ref);

        //take back a bogus value
        Integer bogusRef = 20;
        spawned_takeback(bogusRef);
        assertHasRef(ref);

        //now bring back the correct reference
        spawned_takeback(ref);
        assertHasRef(ref);
    }

    public void testTakebackSuccess() throws InterruptedException {
        Integer ref = 10;
        lendableRef = new RelaxedLendableReference<Integer>(ref);
        spawned_take(ref);
        spawned_takeback(ref);
        assertHasRef(ref);
    }
}