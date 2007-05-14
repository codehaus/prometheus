package org.codehaus.prometheus.references;

/**
 * Unittests {@link org.codehaus.prometheus.references.StrictLendableReference#takebackAndReset(Object)}.
 *
 * @author Peter Veentjer
 *
 * todo: peek checken ivm takebackandreset
 */
public class StrictLendableReference_TakebackAndResetTest extends StrictLendableReference_AbstractTest<Integer> {

    //null can't be returned
    public void testNull() {
        Integer ref = 10;
        lendableRef = new StrictLendableReference(ref);

        tested_take(ref);

        Integer newRef = 20;
        PutThread putThread = tested_pendingPut(newRef);

        //make sure that a null-takeback leads to a NullPointerException
        TakebackAndResetThread<Integer> takebackAndResetThread = scheduleTakebackAndReset(null);
        joinAll(takebackAndResetThread);
        takebackAndResetThread.assertIsTerminatedWithThrowing(NullPointerException.class);
        assertHasRef(ref);
        putThread.assertIsStarted();

        //now do a correct takeback
        takebackAndResetThread = scheduleTakebackAndReset(ref);
        joinAll(takebackAndResetThread);
        takebackAndResetThread.assertIsTerminatedWithoutThrowing();

        // and check that the put has finished
        giveOthersAChance();
        //the takebackandreset has reset the reference to null, so null is returned when the put executes
        putThread.assertSuccess(null);
        putThread.assertIsTerminatedWithoutThrowing();
        assertHasRef(newRef);
    }

    public void testIllegalReference_firstTime() {
       /*
        Integer ref = 10;
        lendableRef = new StrictLendableReference(ref);

        tested_take(ref);

        Integer newRef = 20;
        PutThread putThread = schedulePut(newRef);
        giveOthersAChance();
        putThread.assertIsStarted();

        Integer badRef = 30;
        TakebackAndResetThread takebackAndResetThread = scheduleTakebackAndReset(badRef);
        joinAll(takebackAndResetThread);
        takebackAndResetThread.assertIsTerminatedWithThrowing(IllegalTakebackException.class);
        assertHasRef(ref);
        
        Integer badRef = 30;
                TakebackAndResetThread takebackAndResetThread = scheduleTakebackAndReset(badRef);
                joinAll(takebackAndResetThread);
                takebackAndResetThread.assertIsTerminatedWithThrowing(IllegalTakebackException.class);
                assertHasRef(ref);



        // and check that the put has finished
        giveOthersAChance();
        //the takebackandreset has reset the reference to null, so null is returned when the put executes
        putThread.assertSuccess(null);
        putThread.assertIsTerminatedWithoutThrowing();
        assertHasRef(newRef);*/
    }

    public void testIllegalReference_afterResetIsCalled() {

    }

    public void testHappyFlow_simple() {
        Integer ref = 10;
        lendableRef = new StrictLendableReference(ref);

        //
        tested_take(ref);

        //do a pending put
        Integer newRef = 20;
        PutThread putThread = tested_pendingPut(newRef);

        //do the takeback
        TakebackAndResetThread takebackAndResetThread = scheduleTakebackAndReset(ref);
        joinAll(takebackAndResetThread);
        takebackAndResetThread.assertIsTerminatedWithoutThrowing();

        // and check that the put has finished
        giveOthersAChance();
        //the takebackandreset has reset the reference to null, so null is returned when the put executes
        putThread.assertSuccess(null);
        putThread.assertIsTerminatedWithoutThrowing();
        assertHasRef(newRef);
    }


    public void testHappyFlow_complex() {

    }

    public void testTackbackTooMany() {

    }
}
