/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import org.codehaus.prometheus.concurrenttesting.ConcurrentTestCase;
import org.codehaus.prometheus.concurrenttesting.TestThread;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.joinAll;

import java.util.concurrent.TimeoutException;

public abstract class RelaxedLendableReference_AbstractTest<E> extends ConcurrentTestCase {

    public volatile RelaxedLendableReference<E> lendableRef;

    public void assertHasRef(E expectedRef) {
        assertEquals(expectedRef, lendableRef.peek());
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

    public void spawned_put(E newRef){
        E oldRef = lendableRef.peek();
        PutThread<E> t = schedulePut(newRef);
        joinAll(t);
        t.assertSuccess(oldRef);
    }

    public PutThread schedulePut(E ref){
        PutThread t = new PutThread(lendableRef,ref);
        t.start();
        return t;
    }

    public void spawned_takeback(E takenbackRef) {
        TakeBackThread<Integer> t = scheduleTakeBack(takenbackRef);
        joinAll(t);
        t.assertSuccess();
    }

    public void spawned_takebackAndReset(E takenbackRef) {
        TakebackAndResetThread t = scheduleTakebackAndReset(takenbackRef);
        joinAll(t);
        t.assertIsTerminatedNormally();
    }

    public void spawned_take(Integer expectedTakenRef) {
        TakeThread t = scheduleTake();
        joinAll(t);
        t.assertSuccess(expectedTakenRef);
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
