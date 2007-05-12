/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import junit.framework.TestCase;
import org.codehaus.prometheus.util.ConcurrencyUtil;
import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.references.LendableReference;
import org.codehaus.prometheus.references.IllegalTakebackException;

import java.util.concurrent.TimeUnit;

public class LendThread<E> extends TestThread {
    private final LendableReference<E> lendableRef;
    private final long lendPeriod;
    private final TimeUnit lendUnit;
    private volatile LendState  lendState;
    private volatile E takenRef;
    private final E takebackRef;


    public LendThread(LendableReference<E> lendableRef,E takebackRef, long lendPeriod, TimeUnit lendUnit){
        this.lendableRef = lendableRef;
        this.lendPeriod = lendPeriod;
        this.lendUnit = lendUnit;
        this.takebackRef = takebackRef;
    }

    @Override
    public void runInternal() {
        lendState = LendState.waitingfortake;
        try {
            takenRef = lendableRef.take();
            lendState = LendState.taken;
            ConcurrencyUtil.sleepUninterruptibly(lendPeriod,lendUnit);
            lendableRef.takeback(takebackRef);
            lendState = LendState.takenback;
        } catch (InterruptedException e) {
           lendState = LendState.interrupted;
        }catch(IllegalTakebackException e){
            lendState = LendState.incorrectref;
        }
    }

    public void assertIsWaitingForTake(){
        assertIsTerminatedWithoutThrowing();
        TestCase.assertEquals(LendState.waitingfortake,lendState);
    }

    public void assertIsIncorrectRef(){
        assertIsTerminatedWithoutThrowing();
        TestCase.assertEquals(LendState.incorrectref,lendState);
    }

    public void assertIsTakenBack(E expectedRef){
        assertIsTerminatedWithoutThrowing();
        TestCase.assertEquals(LendState.takenback,lendState);
        TestCase.assertSame(expectedRef, takenRef);
    }

    public void assertIsTaken(E expectedRef){
        assertNoRuntimeException();
        TestCase.assertEquals(LendState.taken,lendState);
        TestCase.assertSame(expectedRef, takenRef);
    }
}
