/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import junit.framework.TestCase;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Unittests the constructors of the {@link DefaultAwaitableReference}.
 *
 * @author Peter Veentjer.
 */
public class DefaultAwaitableReference_ConstructorTest extends TestCase {

    private DefaultAwaitableReference<Integer> awaitableRef;

    //=========== DefaultAwaitableReference() ==================

    public void test_noArg() {
        awaitableRef = new DefaultAwaitableReference<Integer>();

        assertHasRef(null);
        hasDefaultMainLock();
        assertNotNull(awaitableRef.getReferenceAvailableCondition());
    }

    private void hasDefaultMainLock() {
        assertTrue(awaitableRef.getMainLock() instanceof ReentrantLock);
    }

    //=========== DefaultAwaitableReference(E) ==================

    public void test_E_valueIsNull() {
        awaitableRef = new DefaultAwaitableReference<Integer>((Integer) null);

        assertHasRef(null);
        hasDefaultMainLock();
        assertNotNull(awaitableRef.getReferenceAvailableCondition());
    }

    public void assertHasRef(Integer ref){
        assertSame(ref,awaitableRef.peek());
    }

    public void test_E_valueNotNull() {
        Integer ref = 1;
        awaitableRef = new DefaultAwaitableReference<Integer>(ref);

        assertHasRef(ref);
        hasDefaultMainLock();
        assertNotNull(awaitableRef.getReferenceAvailableCondition());
    }

    //=========== DefaultAwaitableReference(Lock) ==================

    public void test_Lock_lockIsNull() {
        try {
            new DefaultAwaitableReference<Integer>((Lock) null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void test_Lock_lockIsNotNull() {
        Lock lock = new ReentrantLock();
        awaitableRef = new DefaultAwaitableReference<Integer>(lock);

        assertHasRef(null);
        assertEquals(lock, awaitableRef.getMainLock());
        assertNotNull(awaitableRef.getReferenceAvailableCondition());
    }

    //=========== DefaultAwaitableReference(E, Lock) ==================

    public void test_E_Lock_lockIsNull() {
        try {
            new DefaultAwaitableReference<Integer>(1, null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void test_E_Lock_valueIsNull() {
        Lock lock = new ReentrantLock();
        Integer ref = null;
        awaitableRef = new DefaultAwaitableReference<Integer>(ref, lock);

        assertHasRef(null);
        assertEquals(lock, awaitableRef.getMainLock());
        assertNotNull(awaitableRef.getReferenceAvailableCondition());
    }

    public void test_E_Lock() {
        Lock lock = new ReentrantLock();
        Integer ref = 10;
        awaitableRef = new DefaultAwaitableReference<Integer>(ref, lock);

        assertHasRef(ref);
        assertEquals(lock, awaitableRef.getMainLock());
        assertNotNull(awaitableRef.getReferenceAvailableCondition());
    }
}
