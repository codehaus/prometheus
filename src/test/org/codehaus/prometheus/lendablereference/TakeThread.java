/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.lendablereference;

import static junit.framework.TestCase.*;
import org.codehaus.prometheus.testsupport.BlockingState;
import org.codehaus.prometheus.testsupport.TestThread;

/**
 * The TakeThread is a thread that tries to tryTake an item
 * from a LendableReference once.
 *
 * @author Peter Veentjer.
 */
public class TakeThread<E> extends TestThread {

    public static <E> TakeThread<E> createStarted(LendableReference<E> lendableRef) {
        TakeThread<E> taker1 = new TakeThread<E>(lendableRef);
        taker1.start();
        return taker1;
    }

    private final LendableReference<E> lendableRef;
    private volatile BlockingState state;
    private volatile E foundRef;

    public TakeThread(LendableReference<E> lendableRef) {
        if(lendableRef == null)throw new NullPointerException();
        this.lendableRef = lendableRef;
    }

    public void runInternal() {
        state = BlockingState.waiting;

        try{
            foundRef = lendableRef.take();
            state = BlockingState.finished;
        }catch(InterruptedException ex){
            state = BlockingState.interrupted;
        }
    }

    public E getFoundRef() {
        return foundRef;
    }

    public BlockingState getBlockingState(){
        return state;
    }

    public void assertInterrupted(){
        assertIsTerminatedWithoutThrowing();
        assertEquals(BlockingState.interrupted,state);
    }

    public void assertSuccess(E expected){
        assertIsTerminatedWithoutThrowing();
        assertEquals(BlockingState.finished,state);
        assertSame(expected,foundRef);
    }
}