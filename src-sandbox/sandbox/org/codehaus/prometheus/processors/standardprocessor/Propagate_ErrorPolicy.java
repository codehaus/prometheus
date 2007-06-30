package org.codehaus.prometheus.processors.standardprocessor;

/**
 * A {@link ErrorPolicy} that propagates the exception to the caller of the evaluate method. No
 * handling is done at all. So policy resembles the (dangerous) situation you normally have.
 *
 * @author Peter Veentjer.
 */
public class Propagate_ErrorPolicy implements ErrorPolicy {
    public Object handleReceiveError(Exception ex, Object in) throws Exception {
        throw ex;
    }
}