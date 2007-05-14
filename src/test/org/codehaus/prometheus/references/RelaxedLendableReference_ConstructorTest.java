/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Unittests the {@link org.codehaus.prometheus.references.RelaxedLendableReference} constructor
 *
 * @author Peter Veentjer
 */
public class RelaxedLendableReference_ConstructorTest extends RelaxedLendableReference_AbstractTest {

    //================ RelaxedLendableReference() =====================

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

    //================ RelaxedLendableReference(Lock) =====================

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

    //================ RelaxedLendableReference(E) =====================

    public void test_E() {
        test_E(null);
        test_E(10);
    }

    private <E> void test_E(E ref) {
        lendableRef = new RelaxedLendableReference<E>(ref);
        assertDefaultLock(lendableRef.getMainLock());
        assertSame(ref, lendableRef.peek());
    }

    //================ RelaxedLendableReference(Lock,E) =====================

    public void test_Reference_E() {
        try {
            new RelaxedLendableReference(10, null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }

        test_Lock_E(new ReentrantLock(), null);
        test_Lock_E(new ReentrantLock(), 10);
    }

    private <E> void test_Lock_E(Lock lock, E ref) {
        lendableRef = new RelaxedLendableReference<E>(ref, lock);
        assertSame(ref, lendableRef.peek());
        assertSame(lock, lendableRef.getMainLock());
    }
}

