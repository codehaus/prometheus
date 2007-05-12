package org.codehaus.prometheus.references;

import org.codehaus.prometheus.testsupport.TestThread;

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
