package org.codehaus.prometheus.resequencer;

/**
 * @author Peter Veentjer.
 */
public class OutOfSpaceException extends RuntimeException {

    public OutOfSpaceException() {
    }

    public OutOfSpaceException(String msg) {
        super(msg);
    }
}
