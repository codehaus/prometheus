/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.testsupport.CountingRunnable;

/**
 * Unittests the {@link ThreadPoolRepeater#start()} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_StartTest extends ThreadPoolRepeater_AbstractTest {

    public void testUnstartedWithoutTask() {
        newUnstartedStrictRepeater();

        repeater.start();

        assertIsRunning();
        assertActualPoolSize(1);
    }

    public void testUnstartedWithTask() {
        CountingRunnable task = new CountingRunnable();
        newUnstartedRepeater(task);

        repeater.start();
        giveOthersAChance();

        assertIsRunning();
        assertActualPoolSize(1);
        task.assertExecutedOnceOrMore();
    }

    public void testRunningWithoutTask() {
        newRunningStrictRepeater();

        assertStartIsIgnored();
        assertActualPoolSize(1);
    }

    public void testStartWithTask() {
        newRunningStrictRepeater(new DummyRepeatable());

        assertStartIsIgnored();
        assertActualPoolSize(1);
    }

    public void testNotStartedWithNoThreads() {
        CountingRunnable task = new CountingRunnable();
        Repeatable repeatable = new RepeatableRunnable(task);

        repeater = new ThreadPoolRepeater(repeatable, 0);
        repeater.start();
        assertIsRunning();

        giveOthersAChance();
        task.assertNotExecuted();
        assertActualPoolSize(0);
    }

    public void testStartWhileShuttingdown() throws InterruptedException {
        newShuttingdownRepeater(2 * DELAY_SMALL_MS);

        assertStartCausesIllegalStateException();
        assertActualPoolSize(1);
    }

    public void testStartWhileShutdown() throws InterruptedException {
        newShutdownRepeater();

        assertStartCausesIllegalStateException();
        assertActualPoolSize(0);
    }

    public void assertStartIsIgnored() {
        RepeaterServiceState oldState = repeater.getState();

        repeater.start();

        assertEquals(oldState, repeater.getState());
    }

    public void assertStartCausesIllegalStateException() {
        RepeaterServiceState oldState = repeater.getState();
        try {
            repeater.start();
            fail("IllegalStateException expected");
        } catch (IllegalStateException ex) {
            assertTrue(true);
        }
        assertEquals(oldState, repeater.getState());
    }
}
