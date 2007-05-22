/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import static junit.framework.Assert.assertSame;
import org.codehaus.prometheus.testsupport.TestThread;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimedTryPutThread<E> extends TestThread {

    private final LendableReference<E> lendableRef;
    private final long timeoutMs;
    private final E ref;
    private volatile E foundRef;

    public TimedTryPutThread(LendableReference<E> lendableRef, E ref, long timeoutMs) {
        this.lendableRef = lendableRef;
        this.timeoutMs = timeoutMs;
        this.ref = ref;
    }

    @Override
    protected void runInternal() throws InterruptedException, TimeoutException {
        foundRef = lendableRef.tryPut(ref, timeoutMs, TimeUnit.MILLISECONDS);
    }

    public void assertSuccess(E expectedReplacement) {
        assertIsTerminatedNormally();
        assertSame(expectedReplacement, foundRef);
    }
}