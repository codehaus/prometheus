package org.codehaus.prometheus.processors.standardprocessor;


/**
 * An {@link ErrorPolicy} that drops drops the message that caused the problem. This policy can
 * be used when it doesn't matter if a message is lost.
 *
 * @author Peter Veentjer.
 */
public class Drop_ErrorPolicy implements ErrorPolicy {
    public Object handleReceiveError(Exception ex, Object in) {
        return null;
    }
}

