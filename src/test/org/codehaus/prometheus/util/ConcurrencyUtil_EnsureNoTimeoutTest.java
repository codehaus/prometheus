package org.codehaus.prometheus.util;

import junit.framework.TestCase;

import java.util.concurrent.TimeoutException;

public class ConcurrencyUtil_EnsureNoTimeoutTest extends TestCase {
    public ConcurrencyUtil_EnsureNoTimeoutTest(String fixture) {
        super(fixture);
    }

    public void test_ensureNoTimeout() {
        test_ensureNoTimeout(10, false);
        test_ensureNoTimeout(0, false);
        test_ensureNoTimeout(-1, true);
    }

    public void test_ensureNoTimeout(long timeoutNs, boolean timeoutExpected) {
        if (timeoutExpected) {
            try {
                ConcurrencyUtil.ensureNoTimeout(timeoutNs);
                fail("TimeoutException expected");
            } catch (TimeoutException ex) {
                assertTrue(true);
            }
        } else {
            try {
                ConcurrencyUtil.ensureNoTimeout(timeoutNs);
                assertTrue(true);
            } catch (TimeoutException e) {
                fail();
            }
        }
    }
}

