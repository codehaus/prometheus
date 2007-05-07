/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

/**
 * Unittests the {@link Latch#tryAwait()} method.
 *
 * @author Peter Veentjer.
 */
public class Latch_TryAwaitTest extends Latch_AbstractTest{

    public void testClosed_startInterrupted(){
        testClosed(START_INTERRUPTED);
    }

    public void testClosed_startUninterrupted(){
        testClosed(START_UNINTERRUPTED);
    }

    public void testClosed(boolean startInterrupted){
        newClosedLatch();

        TryAwaitThread tryAwaitThread = scheduleTryAwait(startInterrupted);

        joinAll(tryAwaitThread);
        tryAwaitThread.assertFailure();
        tryAwaitThread.assertIsTerminatedWithInterruptStatus(startInterrupted);
    }

    public void testOpen_startInterrupted(){
        testOpen(START_INTERRUPTED);
    }

    public void testOpen_startUninterrupted(){
        testOpen(START_UNINTERRUPTED);
    }

    public void testOpen(boolean startInterrupted){
        newOpenLatch();

        TryAwaitThread tryAwaitThread = scheduleTryAwait(startInterrupted);

        joinAll(tryAwaitThread);
        tryAwaitThread.assertSuccess();
        tryAwaitThread.assertIsTerminatedWithInterruptStatus(startInterrupted);
    }
}
