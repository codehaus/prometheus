package org.codehaus.prometheus.processors.standardprocessor;

/**
 * A Policy that passing the exception as message.
 *
 * @author Peter Veentjer.
 */
public class ExceptionAsMessage_ErrorPolicy implements ErrorPolicy{
    public Object handleReceiveError(Exception ex, Object in) throws Exception {
        return ex;
    }
}
