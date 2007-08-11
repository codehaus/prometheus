/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import static junit.framework.Assert.assertEquals;
import org.codehaus.prometheus.testsupport.TestThread;

public class TakeBackThread<E> extends TestThread {

    enum State {
        success, incorrectref
    }

    private final LendableReference<E> lendableRef;
    private final E ref;
    private volatile State state;

    public TakeBackThread(LendableReference<E> lendableRef, E ref) {
        this.lendableRef = lendableRef;
        this.ref = ref;
    }

    @Override
    public void runInternal() {
        try {
            lendableRef.takeback(ref);
            state = State.success;
        } catch (IllegalTakebackException e) {
            state = State.incorrectref;
        }
    }

    public void assertSuccess() {
        assertIsTerminatedNormally();
        assertEquals(State.success, state);
    }

    public void assertIllegalTakeback() {
        assertIsTerminatedNormally();
        assertEquals(State.incorrectref, state);
    }
}
