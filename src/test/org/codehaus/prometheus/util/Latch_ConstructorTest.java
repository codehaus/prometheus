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

    public void test_noArg(){
        Latch latch = new Latch();
        assertFalse(latch.isOpen());
        assertNotNull(latch.getOpenCondition());
        assertTrue(latch.getMainLock() instanceof ReentrantLock);
        ReentrantLock lock = (ReentrantLock)latch.getMainLock();
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
        Latch latch = new Latch(lock);
        assertFalse(latch.isOpen());
        assertSame(lock, latch.getMainLock());
        assertNotNull(latch.getOpenCondition());
    }
}
