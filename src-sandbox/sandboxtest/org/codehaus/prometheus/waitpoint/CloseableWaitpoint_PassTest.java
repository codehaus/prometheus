/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.waitpoint;

import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;

/**
 * Unittests the {@link org.codehaus.prometheus.waitpoint.CloseableWaitpoint#pass()} method.
 *
 * @author Peter Veentjer.
 */
public class CloseableWaitpoint_PassTest extends CloseableWaitpoint_AbstractTest {

    public void testNoWaitingNeeded_startUninterrupted() {
        testNoWaitingNeeded(START_UNINTERRUPTED);
    }

    public void testNoWaitingNeeded_startInterrupted() {
        testNoWaitingNeeded(START_INTERRUPTED);
    }

    public void testNoWaitingNeeded(boolean startInterrupted) {
        newOpenCloseableWaitpoint();

        PassThread t = schedulePass(startInterrupted);
        joinAll(t);

        assertIsOpen();
        t.assertIsTerminatedNormally();
        t.assertIsTerminatedWithInterruptStatus(startInterrupted);
    }

    public void testSomeWaitingNeeded_startInterrupted() {
        newClosedCloseableWaitpoint();

        PassThread passThread = schedulePass(START_INTERRUPTED);

        joinAll(passThread);
        passThread.assertIsInterruptedByException();
    }

    public void testSomeWaitingNeeded() {
        newClosedCloseableWaitpoint();

        PassThread passThread1 = schedulePass();
        PassThread passThread2 = schedulePass();
        giveOthersAChance();

        passThread1.assertIsStarted();
        passThread2.assertIsStarted();

        OpenThread openThread = scheduleOpen();
        joinAll(passThread1, passThread2, openThread);

        assertIsOpen();
        passThread1.assertIsTerminatedNormally();
        passThread2.assertIsTerminatedNormally();
    }

    public void testWaitingTillEndOfTime() {
        newClosedCloseableWaitpoint();
        PassThread passThread = schedulePass();

        giveOthersAChance();
        assertIsClosed();
        passThread.assertIsStarted();
    }

    public void testInterruptedWhileWaiting() {
        newClosedCloseableWaitpoint();

        PassThread passThread = schedulePass();
        giveOthersAChance();

        passThread.interrupt();
        joinAll(passThread);

        passThread.assertIsInterruptedByException();
        assertIsClosed();
    }

    public void testSpuriousWakeups() {
        newClosedCloseableWaitpoint();

        PassThread passThread = schedulePass();
        giveOthersAChance();

        Thread spurious = scheduleSpuriousWakeup();
        joinAll(spurious);
        giveOthersAChance();

        passThread.assertIsStarted();

        OpenThread openThread = scheduleOpen();
        joinAll(openThread);
        giveOthersAChance();

        assertIsOpen();
        passThread.assertIsTerminatedNormally();
    }
}
