/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Unittests the constructors of {@link Latch}.
 *
 * @author Peter Veentjer.
 */
public class Latch_ConstructorTest extends Latch_AbstractTest {

    public void test_noArg() {
        latch = new Latch();

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
            new Latch(null);
            fail();
        } catch (NullPointerException ex) {
            assertTrue(true);
        }

        Lock lock = new ReentrantLock();
        latch = new Latch(lock);

        assertIsClosed();
        assertSame(lock, latch.getMainLock());
        assertNotNull(latch.getOpenCondition());
    }
}
