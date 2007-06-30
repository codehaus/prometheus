/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

/**
 * Unittests the {@link ThreadPoolRepeater#getActualPoolSize()} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_GetActualPoolSizeTest extends ThreadPoolRepeater_AbstractTest {

    //=========== unstarted =============

    public void testUnstarted_strict() {
        testUnstarted(true);
    }

    public void testUnstarted_relaxed() {
        testUnstarted(false);
    }

    public void testUnstarted(boolean strict) {
        newUnstartedRepeater(strict);
        assertActualPoolSize(0);
    }

    //========= running ========

    public void testRunningWithTask_strict() {
        testRunningWithTask(true);
    }

    public void testRunningWithTask_relaxed() {
        testRunningWithTask(false);
    }

    public void testRunningWithTask(boolean strict) {
        newRunningRepeater(strict, new DummyRepeatable());
        assertActualPoolSize(1);
    }

    //========== running without task

    public void testRunningWithoutTask_strict() {
        testRunningWithoutTask(true);
    }

    public void testRunningWithoutTask_relaxed() {
        testRunningWithoutTask(false);
    }

    public void testRunningWithoutTask(boolean strict) {
        newRunningStrictRepeater();
        assertActualPoolSize(1);
    }

    //========== shutting down ================

    public void testShuttingdown_strict() {
        testShuttingdown(true);
    }

    public void testShuttingdown_relaxed() {
        testShuttingdown(false);
    }

    public void testShuttingdown(boolean strict) {
        newShuttingdownRepeater(strict, DELAY_SMALL_MS);
        assertActualPoolSize(1);
    }

    //============= shut down ================

    public void testShutdown_strict() {
        testShutdown(true);
    }

    public void testShutdown_relaxed() {
        testShutdown(false);
    }

    public void testShutdown(boolean strict) {
        newShutdownRepeater(strict);
        assertActualPoolSize(0);
    }
}
