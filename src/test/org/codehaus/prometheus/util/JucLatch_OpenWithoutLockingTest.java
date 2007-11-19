/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.joinAll;

/**
 * Unittests the {@link JucLatch#openWithoutLocking()} method.
 *
 * @author Peter Veentjer.
 */
public class JucLatch_OpenWithoutLockingTest extends JucLatch_AbstractTest {

    public void testOpen_notLockOwner_startUninterrupted() {
        testOpen_notLockOwner(START_UNINTERRUPTED);
    }

    public void testOpen_notLockOwner_startInterrupted() {
        testOpen_notLockOwner(START_INTERRUPTED);
    }

    public void testOpen_notLockOwner(boolean startInterrupted) {
        newOpenLatch();

        OpenWithoutLockingThread openThread = scheduleOpenWithoutLocking(startInterrupted);
        joinAll(openThread);
        assertIsOpen();
        openThread.assertIsTerminatedNormally();
        openThread.assertIsTerminatedWithInterruptFlag(startInterrupted);
    }

    //=====================================

    //=====================================
    //if the latch is closed, we need to signall, this should fail if the
    //calling thread is not the owner of the lock.
    //======================================

    public void testClosed_notLockOwner_startInterrupted() {
        testClosed_notLockOwner(START_INTERRUPTED);
    }

    public void testClosed_notLockOwner_startUninterrupted() {
        testClosed_notLockOwner(START_UNINTERRUPTED);
    }

    public void testClosed_notLockOwner(boolean startInterrupted) {
        newClosedLatch();

        OpenWithoutLockingThread openThread = scheduleOpenWithoutLocking(startInterrupted);
        joinAll(openThread);
        openThread.assertIsTerminatedWithThrowing(IllegalMonitorStateException.class);
        openThread.assertIsTerminatedWithInterruptFlag(startInterrupted);
        assertIsClosed();
    }

    //=====================================================

    public void testClosed_LockOwner() {
        newClosedLatch();
        spawned_open();
    }

    public void testOpen_LockOwner() {
        newOpenLatch();
        spawned_open();
    }
}
