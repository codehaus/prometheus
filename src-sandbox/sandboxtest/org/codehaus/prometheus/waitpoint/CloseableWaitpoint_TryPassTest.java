/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.waitpoint;

import org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil;

/**
 * Unittests the {@link org.codehaus.prometheus.waitpoint.CloseableWaitpoint#tryPass()} method.
 *
 * @author Peter Veentjer.
 */
public class CloseableWaitpoint_TryPassTest extends CloseableWaitpoint_AbstractTest {

    public void testOpen_startUninterrupted() {
        testOpen(false);
    }

    public void testOpen_startInterrupted() {
        testOpen(true);
    }

    public void testOpen(boolean startInterrupted) {
        newOpenCloseableWaitpoint();
        TryPassThread t = scheduleTryPass(startInterrupted);

        ConcurrentTestUtil.joinAll(t);
        t.assertSuccess();
        t.assertIsTerminatedWithInterruptFlag(startInterrupted);
    }


    public void testClosed_startUninterrupted() {
        testClosed(false);
    }

    public void testClosed_startInterrupted() {
        testClosed(true);
    }

    public void testClosed(boolean startInterrupted) {
        newClosedCloseableWaitpoint();
        TryPassThread t = scheduleTryPass(startInterrupted);

        ConcurrentTestUtil.joinAll(t);
        t.assertFailure();
        t.assertIsTerminatedWithInterruptFlag(startInterrupted);
    }
}
