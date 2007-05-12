package org.codehaus.prometheus.references;

import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.references.RelaxedLendableReference;
import org.codehaus.prometheus.references.TakeThread;

import java.util.concurrent.TimeoutException;

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
        TakeThread takeThread = scheduleTake();
        joinAll(takeThread);

        TakebackAndResetThread takebackAndResetThread = scheduleTakebackAndReset(ref);
        joinAll(takebackAndResetThread);
        takebackAndResetThread.assertIsTerminatedWithoutThrowing();

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
        thread.assertIsTerminatedWithoutThrowing();
        assertHasRef(null);
    }

    public void testMultipleIncorrectTackebacks() {
        Integer originalRef = 10;
        lendableRef = new RelaxedLendableReference(originalRef);
        TakeThread takeThread = scheduleTake();
        joinAll(takeThread);

        for (int k = 0; k < 10; k++) {
            Integer replaceRef = 20+k;
            TakebackAndResetThread takebackAndResetThread = scheduleTakebackAndReset(replaceRef);
            joinAll(takebackAndResetThread);
            takebackAndResetThread.assertIsTerminatedWithoutThrowing();
            assertHasRef(null);
        }
    }
}
