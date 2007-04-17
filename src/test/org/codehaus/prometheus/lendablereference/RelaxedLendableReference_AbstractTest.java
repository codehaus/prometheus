/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.lendablereference;

import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.testsupport.TestThread;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class RelaxedLendableReference_AbstractTest<E> extends ConcurrentTestCase {

    public volatile RelaxedLendableReference<E> lendableRef;

    public void assertHasRef(E ref){
        assertEquals(lendableRef.peek(),ref);
    }

    public PutThread schedulePut(E ref){
        PutThread putThread = new PutThread(lendableRef,ref);
        putThread.start();
        return putThread;
    }

    public TakeThread scheduleTake(){
        TakeThread t = new TakeThread(lendableRef);
        t.start();
        return t;
    }

    public TimedTryPutThread scheduleTryPut(E ref, long timeoutMs){
        TimedTryPutThread t = new TimedTryPutThread(lendableRef,ref,timeoutMs,TimeUnit.MILLISECONDS);
        t.start();
        return t;
    }

    public TakebackAndResetThread scheduleTakebackAndReset(E ref){
        TakebackAndResetThread t = new TakebackAndResetThread(ref);
        t.start();
        return t;
    }

    public class TakebackAndResetThread extends TestThread{
        private final E ref;
        public TakebackAndResetThread(E ref) {
            this.ref = ref;
        }

        protected void runInternal() throws InterruptedException, TimeoutException {
            lendableRef.takebackAndReset(ref);
        }
    }
}
