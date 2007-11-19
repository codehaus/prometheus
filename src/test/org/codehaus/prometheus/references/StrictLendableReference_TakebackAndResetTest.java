/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.joinAll;

/**
 * Unittests {@link StrictLendableReference#takebackAndReset(Object)}.
 *
 * @author Peter Veentjer
 *         <p/>
 *         todo: peek checken ivm takebackandreset
 */
public class StrictLendableReference_TakebackAndResetTest extends StrictLendableReference_AbstractTest<Integer> {

    //null can't be returned
    public void testTakebackNullReference() {
        Integer ref = 10;
        lendableRef = new StrictLendableReference(ref);

        try {
            lendableRef.takebackAndReset(null);
            fail();
        } catch (NullPointerException ex) {
        }
    }

    public void testIllegalReference_firstTime() {
        Integer originalRef = 10;
        lendableRef = new StrictLendableReference(originalRef);

        spawned_take(originalRef);

        //do a put and make sure it is blocking
        Integer newRef = 20;
        PutThread putThread = schedulePut(newRef);
        giveOthersAChance();
        putThread.assertIsStarted();

        //first try to do a bad takebackandreset
        Integer badRef = 30;
        assertTakebackAndResetIsRejected(badRef);
        assertHasRef(originalRef);
        assertLendCount(1);

        //do the good takeback and make sure that the structure wasn't corrupted by the bad takeback
        spawned_takebackAndReset(originalRef);
        // and check that the put has finished
        giveOthersAChance();
        //the takebackandreset has reset the reference to null, so null is returned
        //when the put executes
        putThread.assertSuccess(null);
        putThread.assertIsTerminatedNormally();
        assertHasRef(newRef);
        assertLendCount(0);
    }

    public void testIllegalReference_afterResetIsCalled() {
        Integer originalRef = 10;
        lendableRef = new StrictLendableReference(originalRef);

        spawned_take(originalRef);
        spawned_take(originalRef);

        //do a put and make sure it is blocking
        Integer newRef = 20;
        PutThread putThread = schedulePut(newRef);
        giveOthersAChance();
        putThread.assertIsStarted();

        //do the first good takebackandreset
        spawned_takebackAndReset(originalRef);
        //now do a second bad takebackandreset
        Integer badRef = 30;
        assertTakebackAndResetIsRejected(badRef);
        assertHasRef(null);

        //now do a second good takeback
        spawned_takebackAndReset(originalRef);
        // and check that the put has finished
        giveOthersAChance();
        //the takebackandreset has reset the reference to null, so null is returned
        //when the put executes
        putThread.assertSuccess(null);
        putThread.assertIsTerminatedNormally();
        assertHasRef(newRef);
    }

    public void testHappyFlow_simple() {
        Integer ref = 10;
        lendableRef = new StrictLendableReference(ref);

        //do a take
        spawned_take(ref);

        //do a put and make sure it is pending
        Integer newRef = 20;
        PutThread putThread = spawned_pendingPut(newRef);

        //do the takeback
        spawned_takebackAndReset(ref);

        // and check that the put has finished
        giveOthersAChance();
        //the takebackandreset has reset the reference to null, so null is returned when the put executes
        putThread.assertSuccess(null);
        putThread.assertIsTerminatedNormally();
        assertHasRef(newRef);
    }


    public void testHappyFlow_complex() {
        Integer ref = 10;
        lendableRef = new StrictLendableReference(ref);

        //do a take
        spawned_take(ref);

        //do a put and make sure it is pending
        Integer newRef = 20;
        PutThread putThread = spawned_pendingPut(newRef);

        //do the takeback
        spawned_takebackAndReset(ref);

        // and check that the put has finished
        giveOthersAChance();
        //the takebackandreset has reset the reference to null, so null is returned when the put executes
        putThread.assertSuccess(null);
        putThread.assertIsTerminatedNormally();
        assertHasRef(newRef);
    }

    private void assertTakebackAndResetIsRejected(Integer ref) {
        TakebackAndResetThread<Integer> resetThread = scheduleTakebackAndReset(ref);
        joinAll(resetThread);
        resetThread.assertIsTerminatedWithThrowing(IllegalTakebackException.class);
    }

    public void testTooManyTakebacks_simple() {
        Integer ref = 10;
        lendableRef = new StrictLendableReference<Integer>(ref);
        assertTakebackAndResetIsRejected(ref);
    }

    public void testTooManyTakebacks_complex() {
        Integer ref = 10;
        lendableRef = new StrictLendableReference<Integer>(ref);
        spawned_take(ref);

        spawned_takebackAndReset(ref);

        //now do the second takeback and reset and make sure it fails
        assertTakebackAndResetIsRejected(ref);
    }
}
