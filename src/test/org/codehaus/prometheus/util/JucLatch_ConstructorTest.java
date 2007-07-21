/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Unittests the constructors of {@link JucLatch}.
 *
 * @author Peter Veentjer.
 */
public class JucLatch_ConstructorTest extends JucLatch_AbstractTest {

    public void test_noArg() {
        latch = new JucLatch();

        assertIsClosed();
        assertHasDefaultLock();
        assertNotNull(latch.getOpenCondition());
    }

    private void assertHasDefaultLock() {
        assertTrue(latch.getMainLock() instanceof ReentrantLock);
        ReentrantLock lock = (ReentrantLock) latch.getMainLock();
        assertFalse(lock.isFair());
    }

    public void test_Lock() {
        try {
            new JucLatch(null);
            fail();
        } catch (NullPointerException ex) {
            assertTrue(true);
        }

        Lock lock = new ReentrantLock();
        latch = new JucLatch(lock);

        assertIsClosed();
        assertSame(lock, latch.getMainLock());
        assertNotNull(latch.getOpenCondition());
    }
}
