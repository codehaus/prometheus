/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.awaitablereference;

/**
 * Unittests the {@link DefaultAwaitableReference#tryTake()} method.
 *
 * @author Peter Veentjer.
 */
public class DefaultAwaitableReference_TryTakeTest extends DefaultAwaitableReference_AbstractTests{

    public void testReferenceAvailable_startInterrupted(){
        testReferenceAvailable(true);
    }

    public void testReferenceAvailable_startUninterrupted(){
        testReferenceAvailable(false);
    }

    public void testReferenceAvailable(boolean startInterrupted){
        Integer ref = 20;
        awaitableRef = new DefaultAwaitableReference<Integer>(ref);

        TryTakeThread t = scheduleTryTake(startInterrupted);
        joinAll(t);

        t.assertSuccess(ref);
        t.assertIsTerminatedWithInterruptStatus(startInterrupted);
    }

    //=======================================================

    public void testNoReferenceAvailable_startInterrupted(){
        testReferenceAvailable(true);
    }

    public void testNoReferenceAvailable_startUninterrupted(){
        testReferenceAvailable(false);
    }

    public void testNoReferenceAvailable(boolean startInterrupted){
        awaitableRef = new DefaultAwaitableReference<Integer>();

        TryTakeThread t = scheduleTryTake();
        joinAll(t);

        t.assertSuccess(null);
        t.assertIsTerminatedWithInterruptStatus(startInterrupted);
    }
}
