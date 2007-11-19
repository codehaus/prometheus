package org.codehaus.prometheus.processors.standardprocessor;

/**
 * An {@link ErrorPolicy} that replaces the message by the exception, so the exception
 * is the new message.
 *
 * @author Peter Veentjer
 */
public class Exception_ErrorPolicy implements ErrorPolicy {

    public Object handleReceiveError(Exception ex, Object in) throws Exception {
        return ex;
    }
}
