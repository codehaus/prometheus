/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

/**
 * A Runnable that can be extended (to be used for testing purposes) and adds some
 * assertions methods.
 *
 * @author Peter Veentjer.
 */
///CLOVER:OFF
public class TestRunnable extends RunSupport implements Runnable {

    public void runInternal() {
    }

    public final void run() {
        try {
            beginExecutionCount.incrementAndGet();
            try {
                runInternal();
            } catch (RuntimeException ex) {
                this.foundException = ex;
            }
        } finally {
            executedCount.incrementAndGet();
        }
    }
}
