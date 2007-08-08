/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertSame;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class RunSupport {

    protected volatile RuntimeException foundException;
    protected final AtomicInteger beginExecutionCount = new AtomicInteger();
    protected final AtomicInteger executedCount = new AtomicInteger();

    public void assertNotExecuted() {
        assertExecutedCount(0);
    }

    public void assertExecutedOnce() {
        assertExecutedCount(1);
    }

    public void assertExecutedCount(int count) {
        assertEquals(count, executedCount.intValue());
    }

    public void assertBeginExecutionCount(int expected) {
        if (expected < 0) throw new IllegalArgumentException();
        assertEquals(expected, beginExecutionCount.intValue());
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
    public void assertRuntimeException() {
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

    public void assertSameRuntimeException(RuntimeException expectedException) {
        assertNotNull(foundException);
        assertSame(expectedException, foundException);
        foundException.printStackTrace();
    }
}
