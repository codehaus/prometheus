package org.codehaus.prometheus.processors;

/**
 * The DropPolicy drops the input that caused the problem. No outgoing signal
 * will be send. This policy can be used when it doesn't matter if data is lost.
 */
public class DropPolicy implements Policy {
    public Object handle(Exception ex, Object... in) {
        return null;
    }
}

