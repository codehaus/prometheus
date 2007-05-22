/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import static junit.framework.TestCase.assertSame;
import org.codehaus.prometheus.testsupport.TestThread;

/**
 * The TakeThread is a thread that tries to tryTake an item
 * from a LendableReference once.
 *
 * @author Peter Veentjer.
 */
public class TakeThread<E> extends TestThread {

    private final LendableReference<E> lendableRef;
    private volatile E foundRef;

    public TakeThread(LendableReference<E> lendableRef) {
        this.lendableRef = lendableRef;
    }

    @Override
    public void runInternal() throws InterruptedException {
        foundRef = lendableRef.take();
    }

    public E getTakenRef() {
        return foundRef;
    }

    public void assertSuccess(E expected) {
        assertIsTerminatedNormally();
        assertSame(expected, foundRef);
    }
}