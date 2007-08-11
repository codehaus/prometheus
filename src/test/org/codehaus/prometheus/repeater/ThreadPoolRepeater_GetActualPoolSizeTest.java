/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.testsupport.Delays;

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
        newRunningRepeater(strict, new DummyRepeatable(),1);
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

    public void testWhileShuttingdown_strict() {
        testWhileShuttingdown(true);
    }

    public void testWhileShuttingdown_relaxed() {
        testWhileShuttingdown(false);
    }

    public void testWhileShuttingdown(boolean strict) {
        newShuttingdownRepeater(strict, Delays.SMALL_MS);
        assertActualPoolSize(1);
    }

    //=============== forced shuttingdown ==============

    public void testWhileForcedShuttingdown() {
        newForcedShuttingdownRepeater(Delays.MEDIUM_MS,1);
        assertActualPoolSize(1);
    }

    //============= shut down ================

    public void testWhileShutdown_strict() {
        testWhileShutdown(true);
    }

    public void testWhileShutdown_relaxed() {
        testWhileShutdown(false);
    }

    public void testWhileShutdown(boolean strict) {
        newShutdownRepeater(strict);
        assertActualPoolSize(0);
    }
}
