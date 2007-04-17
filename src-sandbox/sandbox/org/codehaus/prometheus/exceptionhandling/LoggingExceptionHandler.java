package org.codehaus.prometheus.exceptionhandling;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;

/**
 * An ExceptionHandler that logs exceptions.
 *
 * LoggingExceptionHandler should be placed up in the callstack. Maybe one of the highest.
 *
 * @author Peter Veentjer.
 */
public class LoggingExceptionHandler implements ExceptionHandler {

    public void handle(Exception ex) {
        //To change body of implemented methods use File | Settings | File Templates.
        //do logging under a try catch and log the exception to the system.err
        //don't propagate it. log the logging exception and log the original exception.
    }
}
