package org.codehaus.prometheus.processors.standardprocessor;

/**
 * The IgnorePolicy ignores that an error occurred, and if input was send to the process
 * that caused the exception, this input will be send to output. The consequence is that no messages
 * are lost, but other processes will also not know that something went wrong.
 *
 * @author Peter Veentjer.
 */
public class IgnorePolicy implements Policy {
    public Object handle(Exception ex, Object... in) {
        return org.codehaus.prometheus.processors.VoidValue.INSTANCE;
    }
}

