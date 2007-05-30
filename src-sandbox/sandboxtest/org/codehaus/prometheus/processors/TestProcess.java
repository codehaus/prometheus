package org.codehaus.prometheus.processors;

import junit.framework.TestCase;

public class TestProcess implements Process {

    public boolean called = false;

    public void assertNotCalled() {
        TestCase.assertFalse(called);
    }

    public void assertCalled(){
        TestCase.assertTrue(called);
    }
}
