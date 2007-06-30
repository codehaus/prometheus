package org.codehaus.prometheus.processors;

/**
 * A Process that returns the message it accepts.
 *
 * @author Peter Veentjer.
 */
public class IdentityProcess extends TestProcess {
    public Object receive(Object msg) {
        signalCalled();
        return msg;
    }
}
