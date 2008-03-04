/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.concurrenttesting;

import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.sleepMs;

import java.util.concurrent.Callable;

/**
 * @author Peter Veentjer.
 */
public class TestCallable<E> extends RunSupport implements Callable<E> {

    private final E result;
    private final Exception throwingException;
    private volatile long delayMs = 0;

    public TestCallable(E result) {
        this.result = result;
        this.throwingException = null;
    }

    public TestCallable(Exception throwingException) {
        if (throwingException == null) throw new NullPointerException();
        this.throwingException = throwingException;
        this.result = null;
    }

    public long getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(long delayMs) {
        this.delayMs = delayMs;
    }

    public E getResult() {
        return result;
    }

    public E call() throws Exception {
        beginExecutionCount.incrementAndGet();
        try {
            if (delayMs > 0)
                sleepMs(delayMs);

            if (throwingException != null)
                throw throwingException;

            return result;
        } finally {
            executedCount.incrementAndGet();
        }
    }
}
