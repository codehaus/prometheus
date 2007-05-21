/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

/**
 * Unittests the {@link org.codehaus.prometheus.references.DefaultAwaitableReference#tryTake()} method.
 *
 * @author Peter Veentjer.
 */
public class DefaultAwaitableReference_TryTakeTest extends DefaultAwaitableReference_AbstractTest {

    public void testReferenceAvailable_startInterrupted(){
        testReferenceAvailable(START_INTERRUPTED);
    }

    public void testReferenceAvailable_startUninterrupted(){
        testReferenceAvailable(START_UNINTERRUPTED);
    }

    public void testReferenceAvailable(boolean startInterrupted){
        Integer ref = 20;
        awaitableRef = new DefaultAwaitableReference<Integer>(ref);

        //do a timed take and make sure it completes with the expected reference
        TryTakeThread t = scheduleTryTake(startInterrupted);
        joinAll(t);
        t.assertSuccess(ref);
        t.assertIsTerminatedWithInterruptStatus(startInterrupted);
    }

    //=======================================================

    public void testNoReferenceAvailable_startInterrupted(){
        testNoReferenceAvailable(START_INTERRUPTED);
    }

    public void testNoReferenceAvailable_startUninterrupted(){
        testNoReferenceAvailable(START_UNINTERRUPTED);
    }

    public void testNoReferenceAvailable(boolean startInterrupted){
        awaitableRef = new DefaultAwaitableReference<Integer>();

        //do a trytake and make sure it completes without a reference
        TryTakeThread t = scheduleTryTake(startInterrupted);
        joinAll(t);
        t.assertSuccess(null);
        t.assertIsTerminatedWithInterruptStatus(startInterrupted);
    }
}
