package org.codehaus.prometheus.processors.standardprocessor;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class StandardStopPolicyTest extends TestCase {
    private DefaultStopPolicy stopPolicy;

    public void testConstructor_nullArg() {
        try {
            new DefaultStopPolicy(null);
            fail();
        } catch (NullPointerException ex) {
        }
    }

    public void testNull() {
        stopPolicy = new DefaultStopPolicy();
        try {
            stopPolicy.shouldStop(null);
            fail();
        } catch (NullPointerException ex) {

        }
    }

    public void testNoMatchingClass() {
        stopPolicy = new DefaultStopPolicy();
        assertNotStop("foo");
    }

    public void testEqualClass() {
        stopPolicy = new DefaultStopPolicy(String.class);
        assertStop("foo");
    }

    private void assertStop(Object value) {
        boolean stop = stopPolicy.shouldStop(value);
        assertTrue(stop);
    }

    private void assertNotStop(Object value) {
        boolean stop = stopPolicy.shouldStop(value);
        assertFalse(stop);
    }

    public void testSubclass() {
        stopPolicy = new DefaultStopPolicy(List.class);
        assertStop(new ArrayList());
    }
}
