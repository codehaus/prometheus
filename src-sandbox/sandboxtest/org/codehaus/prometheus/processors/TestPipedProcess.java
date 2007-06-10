package org.codehaus.prometheus.processors;

import static junit.framework.TestCase.*;


public class TestPipedProcess{

    private volatile boolean called = false;
    private volatile Object foundMsg;
    private final Object returnMsg;

    public TestPipedProcess(Object returnMsg){
        this.returnMsg = returnMsg;
    }

    public Object receive(Object msg) throws Exception {
        called = true;
        this.foundMsg = msg;
        return returnMsg;
    }

    public void assertSuccess(Object expectedMsg) {
        assertTrue(called);
        assertSame(expectedMsg, foundMsg);
    }
}
