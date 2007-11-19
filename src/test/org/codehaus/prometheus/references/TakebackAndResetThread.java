/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import org.codehaus.prometheus.concurrenttesting.TestThread;

public class TakebackAndResetThread<E> extends TestThread {
    private final LendableReference<E> lendableRef;
    private final E ref;

    public TakebackAndResetThread(LendableReference<E> lendableRef, E ref) {
        this.lendableRef = lendableRef;
        this.ref = ref;
    }

    @Override
    protected void runInternal() throws Exception {
        lendableRef.takebackAndReset(ref);
    }
}
