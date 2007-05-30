/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.testsupport.TestThread;

import java.util.concurrent.TimeoutException;

public abstract class RelaxedLendableReference_AbstractTest<E> extends ConcurrentTestCase {

    public volatile RelaxedLendableReference<E> lendableRef;

    public void assertHasRef(E ref) {
        assertEquals(lendableRef.peek(), ref);
    }

    public TakeThread scheduleTake() {
        TakeThread t = new TakeThread(lendableRef);
        t.start();
        return t;
    }

    public TakeBackThread scheduleTakeBack(E ref) {
        TakeBackThread t = new TakeBackThread(lendableRef, ref);
        t.start();
        return t;
    }

    public TakebackAndResetThread scheduleTakebackAndReset(E ref) {
        TakebackAndResetThread t = new TakebackAndResetThread(ref);
        t.start();
        return t;
    }

    public void _tested_takeback(E takenbackRef) {
        TakeBackThread<Integer> takebackThread1 = scheduleTakeBack(takenbackRef);
        joinAll(takebackThread1);
        takebackThread1.assertSuccess();
    }

    public void _tested_takebackAndReset(E takenbackRef) {
        TakebackAndResetThread takebackAndResetThread = scheduleTakebackAndReset(takenbackRef);
        joinAll(takebackAndResetThread);
        takebackAndResetThread.assertIsTerminatedNormally();
    }

    public void _tested_take(Integer expectedTakenRef) {
        TakeThread takeThread = scheduleTake();
        joinAll(takeThread);
        takeThread.assertSuccess(expectedTakenRef);
    }

    public class TakebackAndResetThread extends TestThread {
        private final E ref;

        public TakebackAndResetThread(E ref) {
            this.ref = ref;
        }

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            lendableRef.takebackAndReset(ref);
        }
    }
}
