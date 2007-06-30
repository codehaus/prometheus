package org.codehaus.prometheus.processors.standardprocessor;

import org.codehaus.prometheus.processors.VoidValue;

/**
 * An ErrorPolicy that ignores that an error occurred, and if input was send to the process
 * that caused the exception, this input will be send to output. The consequence is that no messages
 * are lost, but other processes will also not know that something went wrong.
 *
 * @author Peter Veentjer.
 */
public class Ignore_ErrorPolicy implements ErrorPolicy {
    public Object handleReceiveError(Exception ex, Object in) {
        return VoidValue.INSTANCE;
    }
}

