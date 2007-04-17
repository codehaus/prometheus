/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import static junit.framework.TestCase.*;

/**
 * A Runnable that can be extended (to be used for testing purposes) and adds some
 * assertions methods.
 *
 * @author Peter Veentjer.
 */
public abstract class TestRunnable implements Runnable {

    private volatile RuntimeException foundException;

    public abstract void runInternal();

    public final void run() {
        try {
            runInternal();
        } catch (RuntimeException ex) {
            this.foundException = ex;
        }
    }

    /**
     * Returns the (last) thrown foundThrowable. If no foundThrowable is thrown,
     * null is returned.
     *
     * @return the last thrown foundThrowable.
     */
    public RuntimeException getException() {
        return foundException;
    }

    /**
     * Assert that no RuntimeException was thrown. If one is thrown, the stacktrace is also printed.
     */
    public void assertNoRuntimeException() {
        if (foundException != null)
            foundException.printStackTrace();
        assertNull(foundException);
    }

    /**
     * Asserts that a RuntimeException is thrown.
     */
    public void assertRuntimeException(){
        assertNotNull(foundException);
    }

    /**
     * Assert that a RuntimeExeption was thrown, and that its type
     * is the expected type.
     *
     * @param exceptionClazz
     */
    public void assertRuntimeException(Class exceptionClazz) {
        assertNotNull(foundException);
        assertTrue(exceptionClazz.isInstance(foundException));
        foundException.printStackTrace();
    }

    public void assertSameRuntimeException(RuntimeException expectedException){
        assertNotNull(foundException);
        assertSame(expectedException,foundException);
        foundException.printStackTrace();
    }
}
