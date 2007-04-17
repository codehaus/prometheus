/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.lendablereference;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RelaxedLendableReference_ConstructorTest extends RelaxedLendableReference_AbstractTest {
    public void testNoArg() {
        lendableRef = new RelaxedLendableReference<Integer>();
        assertNull(lendableRef.peek());
        assertDefaultLock(lendableRef.getMainLock());
    }

    private void assertDefaultLock(Lock lock) {
        assertNotNull(lock);
        assertTrue(lock instanceof ReentrantLock);
        ReentrantLock reLock = (ReentrantLock) lock;
        assertFalse(reLock.isFair());
    }

    public void test_Lock() {
        try {
            new RelaxedLendableReference<Integer>((Lock) null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }

        Lock lock = new ReentrantLock();
        lendableRef = new RelaxedLendableReference<Integer>(lock);
        assertSame(lock, lendableRef.getMainLock());
        assertNull(lendableRef.peek());
    }

    public void test_Reference() {
        test_Reference(null);
        test_Reference(10);
    }

    private <E> void test_Reference(E ref) {
        lendableRef = new RelaxedLendableReference<E>(ref);
        assertDefaultLock(lendableRef.getMainLock());
        assertSame(ref, lendableRef.peek());
    }

    public void test_Lock_Reference() {
        try {
            new RelaxedLendableReference(10, null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }

        test_Lock_Reference(new ReentrantLock(), null);
        test_Lock_Reference(new ReentrantLock(), 10);
    }

    private <E> void test_Lock_Reference(Lock lock, E ref) {
        lendableRef = new RelaxedLendableReference<E>(ref, lock);
        assertSame(ref, lendableRef.peek());
        assertSame(lock, lendableRef.getMainLock());
    }
}

