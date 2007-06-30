package org.codehaus.prometheus.processors;

import static junit.framework.Assert.assertEquals;

public class IntegerExceptionProcess extends TestProcess {
    private final Exception ex;
    private final Error error;
    private Integer expected;

    public IntegerExceptionProcess(Integer expected, Exception ex) {
        this.expected = expected;
        this.ex = ex;
        this.error = null;
    }

    public IntegerExceptionProcess(Integer expected, Error error) {
        this.expected = expected;
        this.error = error;
        this.ex = null;
    }

    public void receive(Integer value) throws Exception {
        assertEquals(expected, value);
        signalCalled();

        if (error != null)
            throw error;
        else
            throw ex;
    }
}
