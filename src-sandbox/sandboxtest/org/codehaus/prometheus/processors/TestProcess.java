package org.codehaus.prometheus.processors;

import static junit.framework.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Process that can be used for testing purposes. 
 *
 * @author Peter Veentjer.
 */
public class TestProcess {

    public final AtomicInteger callCount = new AtomicInteger();

    public void signalCalled() {
        callCount.incrementAndGet();
    }

    public void assertCalled(int count) {
        assertEquals(count, callCount.intValue());
    }

    public void assertNotCalled() {
        assertCalled(0);
    }

    public void assertCalledOnce() {
        assertCalled(1);
    }
}
