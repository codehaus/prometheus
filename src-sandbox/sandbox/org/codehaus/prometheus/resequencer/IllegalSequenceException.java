package org.codehaus.prometheus.resequencer;

/**
 * @author Peter Veentjer.
 */
public class IllegalSequenceException extends RuntimeException {

    public IllegalSequenceException() {
    }

    public IllegalSequenceException(String msg) {
        super(msg);
    }
}
