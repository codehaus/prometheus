package org.codehaus.prometheus.processors.standardprocessor;


/**
 * An {@link ErrorPolicy} that drops drops the input that caused the problem. No outgoing signal
 * will be send. This policy can be used when it doesn't matter if data is lost.
 *
 * @author Peter Veentjer.
 */
public class DropPolicy implements ErrorPolicy {
    public Object handle(Exception ex, Object... in) {
        return null;
    }
}

