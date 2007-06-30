/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import static junit.framework.TestCase.assertSame;
import org.codehaus.prometheus.testsupport.TestThread;

import java.util.concurrent.TimeoutException;

/**
 * @author Peter Veentjer.
 */
public class PutThread<E> extends TestThread {
    private final LendableReference<E> lendableRef;
    private final E newRef;
    private volatile E replacedRef;

    public PutThread(LendableReference<E> lendableRef, E newRef) {
        this.lendableRef = lendableRef;
        this.newRef = newRef;
    }

    @Override
    protected void runInternal() throws InterruptedException, TimeoutException {
        replacedRef = lendableRef.put(newRef);
    }

    public void assertSuccess(E expectedReplacedRef) {
        assertIsTerminatedNormally();
        assertSame(expectedReplacedRef, replacedRef);
    }
}
