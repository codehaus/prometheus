/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import java.util.concurrent.Callable;

public class TestCallable<E> extends RunSupport implements Callable<E> {
    private final E result;

    public TestCallable(E result) {
        this.result = result;
    }

    public E getResult() {
        return result;
    }

    public E call() throws Exception {
        return result;
    }

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
