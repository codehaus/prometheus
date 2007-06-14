package org.codehaus.prometheus.processors;

/**
 * The ThrowPolicy doesn't handle the exception at all. The exception is
 * propagated to the once() caller.
 *
 * @author Peter Veentjer.
 */
public class ThrowPolicy implements Policy {
    public Object handle(Exception ex, Object... in) throws Exception {
        throw ex;
    }
}