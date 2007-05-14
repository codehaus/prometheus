/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

/**
 * Unittests the {@link org.codehaus.prometheus.references.StrictLendableReference#tryTake()} method.
 *
 * Peter Veentjer.
 */
public class StrictLendableReference_TryTakeTest extends StrictLendableReference_AbstractTest<Integer> {

    public void testRefAvailable_startInterrupted(){
        testRefAvailable(true);
    }

    public void testRefAvailable_startUninterrupted(){
        testRefAvailable(false);
    }

    public void testRefAvailable(boolean startInterrupted){
        Integer ref = 10;
        lendableRef = new StrictLendableReference<Integer>(ref);

        //using two thread also tests that takes don't exclude each other.
        TryTakeThread<Integer> t1 = scheduleTryTake(startInterrupted);
        TryTakeThread<Integer> t2 = scheduleTryTake(startInterrupted);
        joinAll(t1,t2);
        t1.assertSuccess(ref);
        t1.assertSuccess(ref);
        t1.assertIsTerminatedWithInterruptStatus(startInterrupted);
        t2.assertIsTerminatedWithInterruptStatus(startInterrupted);
        assertHasRef(ref);
        assertLendCount(2);
    }

    //======== some waiting needed =======================

    public void testNoRefAvailable_startInterrupted(){
        testNoRefAvailable(true);
    }

    public void testNoRefAvailable_startUninterrupted(){
        testNoRefAvailable(false);
    }

    public void testNoRefAvailable(boolean startInterrupted){
        lendableRef = new StrictLendableReference<Integer>();

        //do a take and make sure it completes
        TryTakeThread<Integer> t = scheduleTryTake(startInterrupted);
        joinAll(t);
        t.assertSuccess(null);
        t.assertIsTerminatedWithInterruptStatus(startInterrupted);
        assertHasRef(null);
        assertLendCount(0);
    }
}
