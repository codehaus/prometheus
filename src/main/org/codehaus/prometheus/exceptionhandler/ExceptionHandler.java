package org.codehaus.prometheus.exceptionhandler;

/**
 * A handler for exceptions.
 *
 * @author Peter Veentjer.
 */
public interface ExceptionHandler {

    /**
     * Handles the exception.
     *
     * @param ex the exception to handle. This will never be <tt>null</tt>.
     * @throws NullPointerException if ex is null, it is up the the implementation to decide
     *                              if any checking should be done.
     */
    void handle(Exception ex);
}
