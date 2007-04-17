package org.codehaus.prometheus.lendablereference;

import org.codehaus.prometheus.testsupport.TestThread;

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

        TakebackAndResetThread takebackThread = scheduleTakebackAndReset(ref);
        joinAll(takebackThread);
        takebackThread.assertIsTerminated();

        assertHasRef(null);
    }

    public void testTakebackBySameThread(){
        final Integer ref = 10;
        lendableRef = new RelaxedLendableReference(ref);
        TestThread thread = new TestThread(){
            protected void runInternal() throws InterruptedException, TimeoutException {
                Integer ref = lendableRef.take();
                sleepMs(DELAY_SMALL_MS);
                lendableRef.takebackAndReset(ref);
            }
        };
        thread.start();
        joinAll(thread);
        thread.assertIsTerminated();
        assertHasRef(null);
    }

    public void testMultipleIncorrectTackebacks() {
        Integer originalRef = 10;
        lendableRef = new RelaxedLendableReference(originalRef);
        TakeThread takeThread = scheduleTake();
        joinAll(takeThread);

        for (int k = 0; k < 10; k++) {
            Integer replaceRef = 20+k;
            TakebackAndResetThread takebackThread = scheduleTakebackAndReset(replaceRef);
            joinAll(takebackThread);
            takebackThread.assertIsTerminated();
            assertHasRef(null);
        }
    }
}
