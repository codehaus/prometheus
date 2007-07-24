package org.codehaus.prometheus.references;

import org.codehaus.prometheus.testsupport.TestThread;
import static org.codehaus.prometheus.testsupport.TestUtil.sleepMs;

import java.util.concurrent.TimeoutException;

/**
 * Unittests {@link RelaxedLendableReference#takebackAndReset(Object)} method.
 *
 * @author Peter Veentjer.
 */
public class RelaxedLendableReference_TakebackAndResetTest extends RelaxedLendableReference_AbstractTest<Integer> {

    public void test_null() {
        Integer oldRef = 10;
        lendableRef = new RelaxedLendableReference(oldRef);

        try {
            lendableRef.takebackAndReset(null);
            fail();
        } catch (NullPointerException ex) {
        }

        assertHasRef(oldRef);
    }

    public void test_differentInstanceButEqualValue(){
        Integer ref1 = new Integer(10);
        Integer ref2 = new Integer(10);
        lendableRef = new RelaxedLendableReference(ref1);

        spawned_take(ref1);
        spawned_takebackAndReset(ref2);
        assertHasRef(null);
    }

    public void test_byDifferentThread() {
        Integer ref = 10;
        lendableRef = new RelaxedLendableReference(ref);

        spawned_take(ref);
        spawned_takebackAndReset(ref);
        assertHasRef(null);
    }

    public void test_bySameThread() {
        final Integer ref = 10;
        lendableRef = new RelaxedLendableReference(ref);

        TestThread thread = new TestThread() {
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

    //make sure that the new value is not removed
    public void test_differentValueAlreadySet(){
        Integer oldRef = 10;
        Integer newRef = 20;
        lendableRef = new RelaxedLendableReference(oldRef);

        spawned_take(oldRef);
        spawned_put(newRef);
        spawned_takebackAndReset(oldRef);
        //the new value should not be overwritten by a null value.
        assertHasRef(newRef);
    }

    public void test_multiple(){
        Integer ref = 10;
        lendableRef = new RelaxedLendableReference(ref);

        spawned_take(ref);
        spawned_take(ref);
        spawned_takebackAndReset(ref);
        assertHasRef(null);
        spawned_takebackAndReset(ref);
        assertHasRef(null);
        spawned_takeback(ref);
        assertHasRef(null);
        spawned_takebackAndReset(ref);
        assertHasRef(null);
    }

    public void test_differentValueSetInBetween(){
        Integer oldRef = 10;
        Integer newRef = 20;
        lendableRef = new RelaxedLendableReference(oldRef);

        spawned_take(oldRef);
        spawned_put(newRef);
        spawned_put(oldRef);
        spawned_takebackAndReset(oldRef);
        //even though a new value was set in between, the old value was restored eventually
        //so the takeback and reset works.
        assertHasRef(null);
    }

    public void test_MultipleIncorrectTackebacks() {
        Integer originalRef = 10;
        lendableRef = new RelaxedLendableReference(originalRef);

        spawned_take(originalRef);

        spawned_takebackAndReset(originalRef+1);
        assertHasRef(originalRef);
        spawned_takebackAndReset(originalRef+2);
        assertHasRef(originalRef);
        spawned_takebackAndReset(originalRef);
        assertHasRef(null);        
    }
}
