package org.codehaus.prometheus.processors.standardprocessor;

/**
 * A {@link ErrorPolicy} that propages the exception to the caller of the once method. No
 * handling is done at all.
 *
 * @author Peter Veentjer.
 */
public class PropagatePolicy implements ErrorPolicy {
    public Object handle(Exception ex, Object... in) throws Exception {
        throw ex;
    }
}