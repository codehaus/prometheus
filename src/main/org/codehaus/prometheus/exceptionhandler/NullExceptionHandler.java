package org.codehaus.prometheus.exceptionhandler;

/**
 * A dummy ExceptionHandler that doesn't do anything. It can be used as in instance if no
 * other instance is provided.
 *
 * @author Peyer Veentjer.
 */
public class NullExceptionHandler implements ExceptionHandler {

    public static final NullExceptionHandler INSTANCE = new NullExceptionHandler();

    public void handle(Exception ex) {
        //do nothing.
    }
}
