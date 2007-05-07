/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import junit.framework.TestCase;

/**
 * Detects if this runnable was called with the interrupt flag set and next it
 * sets the interrupt flag. This is useful for testing executing structures like a repeater
 * or executor if the unset the interrupt flag before executing.
 *
 * @author Peter Veentjer.
 */
public class InterruptingAndDetectingRunnable extends TestRunnable {

    private volatile boolean interruptfound = false;

    @Override
    public void runInternal() {
        detectInterruptStatus();
        setInterruptStatus();
    }

    /**
     * Sets the interrupt status of the calling thread.
     */
    private void setInterruptStatus() {
        Thread.currentThread().interrupt();
    }

    /**
     * Detects if the interrupt status of the Thread was set. If it was set, the
     * interruptfound is set to true.
     */
    private void detectInterruptStatus() {
        if (Thread.currentThread().isInterrupted()) {
            interruptfound = true;
        }
    }

    /**
     * Returns true if an interrupt was found, false otherwise.
     *
     * @return true if an interrupt was found, false otherwise.
     */
    public boolean isInterruptfound() {
        return interruptfound;
    }

    /**
     * Asserts that no interrupt was found and that no RuntimeException is thrown.
     */
    public void assertNoInterruptFound() {
        assertNoRuntimeException();
        TestCase.assertFalse(interruptfound);
    }
}

