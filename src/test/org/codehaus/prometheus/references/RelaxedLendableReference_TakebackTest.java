/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import org.codehaus.prometheus.references.RelaxedLendableReference;

/**
 * todo: this tests executes the operations on the junitthread and this could lead to
 * problems like not detecting liveness problems for example because the test won't fail (or
 * stall)
 *
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
        Integer lendRef = lendableRef.take();

        lendableRef.takeback(lendRef);        
        //this one is too many
        lendableRef.takeback(lendRef);
    }

    //an incorrect reference can be taken back without problems.
    public void testTakebackIncorrectRefShouldBeIgnored() {
        Integer oldRef = 10;
        lendableRef = new RelaxedLendableReference<Integer>(oldRef);

        Integer bogusRef = 20;
        lendableRef.takeback(bogusRef);

        assertHasRef(oldRef);        
    }

    public void testTakebackSuccess() throws InterruptedException {
        Integer ref = 10;
        lendableRef = new RelaxedLendableReference<Integer>(ref);
        Integer lendRef = lendableRef.take();
        lendableRef.takeback(lendRef);
    }
}