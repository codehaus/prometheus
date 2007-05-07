/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.lendablereference;

import static junit.framework.Assert.assertSame;
import org.codehaus.prometheus.testsupport.TestThread;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimedTryPutThread<E> extends TestThread {

    private final LendableReference<E> lendableRef;
    private final long timeout;
    private final TimeUnit timeoutUnit;
    private final E ref;
    private volatile E foundRef;

    public TimedTryPutThread(LendableReference<E> lendableRef, E ref, long timeout, TimeUnit timeoutUnit) {
        this.lendableRef = lendableRef;
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        this.ref = ref;
    }

    @Override
    protected void runInternal() throws InterruptedException, TimeoutException {
        foundRef = lendableRef.tryPut(ref, timeout, timeoutUnit);
    }

    public E getFoundRef() {
        return foundRef;
    }

    public void assertSuccess(E expectedReplacement) {
        assertIsTerminated();
        assertSame(expectedReplacement, foundRef);
    }
}