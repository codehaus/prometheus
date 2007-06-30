package org.codehaus.prometheus.processors;

import static junit.framework.Assert.assertSame;

/**
 * A TestProcess that matches on an Integer, and sends a value back.
 *
 * @author Peter Veentjer.
 */
public class IntegerProcess extends TestProcess {
    private final Integer expected;
    private final Object returned;

    public IntegerProcess() {
        this(null, null);
    }

    public IntegerProcess(Integer expected, Object returned) {
        this.expected = expected;
        this.returned = returned;
    }

    public Object receive(Integer i) {
        assertSame(expected, i);
        signalCalled();
        return returned;
    }
}