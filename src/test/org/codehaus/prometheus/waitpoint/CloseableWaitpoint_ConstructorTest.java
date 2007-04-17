/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.waitpoint;

import org.codehaus.prometheus.waitpoint.CloseableWaitpoint;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Unittests the constructors for {@link org.codehaus.prometheus.waitpoint.CloseableWaitpoint}.
 *
 * @author Peter Veentjer.
 */
public class CloseableWaitpoint_ConstructorTest extends CloseableWaitpoint_AbstractTest {

    public void test_noArg() {
        waitpoint = new CloseableWaitpoint();
        assertHasDefaultMainLock();
        assertIsOpen();
    }

    public void test_boolean() {
        waitpoint = new CloseableWaitpoint(true);
        assertIsOpen();
        assertHasDefaultMainLock();
    }

    public void test_Lock_boolean() {
        try {
            new CloseableWaitpoint(null, true);
            fail();
        } catch (NullPointerException ex) {
            assertTrue(true);
        }

        boolean open = true;
        Lock mainLock = new ReentrantLock();
        waitpoint = new CloseableWaitpoint(mainLock, open);
        assertIsOpen(true);
        assertSame(mainLock, waitpoint.getMainLock());
    }

    public void assertHasDefaultMainLock() {
        Lock lock = waitpoint.getMainLock();
        assertNotNull(lock);
        assertTrue(lock instanceof ReentrantLock);
        ReentrantLock reentrantLock = (ReentrantLock) lock;
        assertFalse(reentrantLock.isFair());
    }
}
