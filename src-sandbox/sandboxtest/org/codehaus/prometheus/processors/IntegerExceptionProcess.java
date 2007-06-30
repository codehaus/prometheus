package org.codehaus.prometheus.processors;

import static junit.framework.Assert.assertEquals;

public class IntegerExceptionProcess extends TestProcess {
    private final Exception ex;
    private Integer expected;

    public IntegerExceptionProcess(Integer expected, Exception ex) {
        this.expected = expected;
        this.ex = ex;
    }

    public void receive(Integer value) throws Exception {
        assertEquals(expected, value);
        signalCalled();
        throw ex;
    }
}
