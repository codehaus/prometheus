/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.waitpoint;

/**
 * Unittests the {@link org.codehaus.prometheus.waitpoint.CloseableWaitpoint#tryPass()} method.
 *
 * @author Peter Veentjer.
 */
public class CloseableWaitpoint_TryPassTest extends CloseableWaitpoint_AbstractTest {

    public void testOpen_startUninterrupted(){
        testOpen(false);
    }

    public void testOpen_startInterrupted(){
        testOpen(true);
    }

    public void testOpen(boolean startInterrupted){
        newOpenCloseableWaitpoint();
        TryPassThread t = scheduleTryPass(startInterrupted);

        joinAll(t);
        t.assertSuccess();
        t.assertIsTerminatedWithInterruptStatus(startInterrupted);
    }


    public void testClosed_startUninterrupted(){
        testClosed(false);
    }

    public void testClosed_startInterrupted(){
        testClosed(true);
    }

    public void testClosed(boolean startInterrupted){
        newClosedCloseableWaitpoint();
        TryPassThread t = scheduleTryPass(startInterrupted);

        joinAll(t);
        t.assertFailure();
        t.assertIsTerminatedWithInterruptStatus(startInterrupted);
    }
}
