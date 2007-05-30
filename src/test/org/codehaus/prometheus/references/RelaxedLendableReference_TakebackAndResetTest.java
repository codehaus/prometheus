package org.codehaus.prometheus.references;

import org.codehaus.prometheus.testsupport.TestThread;

import java.util.concurrent.TimeoutException;

/**
 * Unittests {@link RelaxedLendableReference#takebackAndReset(Object)} method.
 *
 * @author Peter Veentjer.
 */
public class RelaxedLendableReference_TakebackAndResetTest extends RelaxedLendableReference_AbstractTest<Integer> {

    public void testNulltakeback() {
        Integer oldRef = 10;
        lendableRef = new RelaxedLendableReference(oldRef);

        try {
            lendableRef.takebackAndReset(null);
            fail();
        } catch (NullPointerException ex) {
        }

        assertHasRef(oldRef);
    }

    public void testTakebackByDifferentThread() {
        Integer ref = 10;
        lendableRef = new RelaxedLendableReference(ref);

        _tested_take(ref);
        _tested_takebackAndReset(ref);
        assertHasRef(null);
    }

    public void testTakebackBySameThread(){
        final Integer ref = 10;
        lendableRef = new RelaxedLendableReference(ref);
        TestThread thread = new TestThread(){
            @Override
            protected void runInternal() throws InterruptedException, TimeoutException {
                Integer ref = lendableRef.take();
                sleepMs(DELAY_SMALL_MS);
                lendableRef.takebackAndReset(ref);
            }
        };
        thread.start();
        joinAll(thread);
        thread.assertIsTerminatedNormally();
        assertHasRef(null);
    }

    public void testMultipleIncorrectTackebacks() {
        Integer originalRef = 10;
        lendableRef = new RelaxedLendableReference(originalRef);

        _tested_take(originalRef);

        //do multiple takebacksAndResets with different reference, and check that the
        //the reference has null
        for (int k = 0; k < 10; k++) {
            Integer replaceRef = 20+k;
            _tested_takebackAndReset(replaceRef);
            assertHasRef(null);
        }
    }
}
