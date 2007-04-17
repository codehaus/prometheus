/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import junit.framework.TestCase;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConcurrencyUtil_ToUsableNanosTest extends TestCase {
    public void testArguments() throws TimeoutException {
        try {
            ConcurrencyUtil.toUsableNanos(1, null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testNegativeTimeout() {
        try {
            ConcurrencyUtil.toUsableNanos(-1, TimeUnit.NANOSECONDS);
            fail("TimeoutException expected");
        } catch (TimeoutException e) {
            assertTrue(true);
        }
    }

    public void testNulTimeout() throws TimeoutException {
        long ns = ConcurrencyUtil.toUsableNanos(0, TimeUnit.NANOSECONDS);
        assertEquals(0, ns);
    }

    public void testPositiveTimeout() throws TimeoutException {
        long ns = ConcurrencyUtil.toUsableNanos(100, TimeUnit.SECONDS);
        assertEquals(TimeUnit.SECONDS.toNanos(100), ns);
    }
}


