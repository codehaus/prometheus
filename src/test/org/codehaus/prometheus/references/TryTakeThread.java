/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import static junit.framework.Assert.assertEquals;
import org.codehaus.prometheus.testsupport.TestThread;

public class TryTakeThread<E> extends TestThread {
    private final LendableReference<E> lendableRef;
    private volatile E foundRef;

    public TryTakeThread(LendableReference<E> lendableRef) {
        this.lendableRef = lendableRef;
    }

    @Override
    protected void runInternal() {
        foundRef = lendableRef.tryTake();
    }

    public void assertSuccess(E expectedRef) {
        assertIsTerminatedNormally();
        assertEquals(expectedRef, foundRef);
    }
}
