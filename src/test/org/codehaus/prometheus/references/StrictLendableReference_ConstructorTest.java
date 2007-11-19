/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Unittests the constructors of the {@link org.codehaus.prometheus.references.StrictLendableReference}.
 * <p/>
 * Peter Veentjer.
 */
public class StrictLendableReference_ConstructorTest extends StrictLendableReference_AbstractTest {

  public void testNoArg() {
        lendableRef = new StrictLendableReference();

        assertHasRef(null);
        assertHasDefaultLock();
        assertNotNull(lendableRef.getNoTakersCondition());
        assertNotNull(lendableRef.getRefAvailableCondition());
        assertLendCount(0);
    }

    private void assertHasDefaultLock() {
        assertNotNull(lendableRef);
        Lock lock = lendableRef.getMainLock();
        assertTrue(lock instanceof ReentrantLock);
        ReentrantLock reentrantLock = (ReentrantLock) lock;
        assertTrue(reentrantLock.isFair());
    }

    private void assertHasFairReentrantLock() {
        assertNotNull(lendableRef);
        Lock lock = lendableRef.getMainLock();
        assertTrue(lock instanceof ReentrantLock);
        ReentrantLock reentrantLock = (ReentrantLock) lock;
        assertTrue(reentrantLock.isFair());
    }

    public void test_E() {
        Integer ref = 10;
        lendableRef = new StrictLendableReference<Integer>(ref);
        assertHasRef(ref);
        assertHasDefaultLock();
        assertNotNull(lendableRef.getNoTakersCondition());
        assertNotNull(lendableRef.getRefAvailableCondition());
        assertLendCount(0);
    }

    public void test_boolean_E() {
        Integer ref = 10;
        lendableRef = new StrictLendableReference<Integer>(true, ref);
        assertHasRef(ref);
        assertHasFairReentrantLock();
        assertNotNull(lendableRef.getNoTakersCondition());
        assertNotNull(lendableRef.getRefAvailableCondition());
        assertLendCount(0);
    }

    public void test_Lock_E() {
        try {
            new StrictLendableReference(null, 10);
            fail("NullPointerException foundThrowable");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }

        test_Lock_E_goodConstruction(new ReentrantLock(), null);
        test_Lock_E_goodConstruction(new ReentrantLock(), 10);
    }


    private <E> void test_Lock_E_goodConstruction(Lock lock, E expectedRef) {
        lendableRef = new StrictLendableReference(lock, expectedRef);
        assertState(lock, expectedRef);
        assertLendCount(0);
    }

    private <E> void assertState(Lock lock, E expectedRef) {
        assertSame(lock, lendableRef.getMainLock());
        assertNotNull(lendableRef.getNoTakersCondition());
        assertNotNull(lendableRef.getRefAvailableCondition());
        assertHasRef(expectedRef);
    }
}
