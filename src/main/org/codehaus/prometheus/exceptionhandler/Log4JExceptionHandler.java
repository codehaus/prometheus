package org.codehaus.prometheus.exceptionhandler;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.Level;

/**
 * An {@link ExceptionHandler} that uses Log4j als logging mechanism.
 *
 * @author Peter Veentjer.
 */
public class Log4JExceptionHandler implements ExceptionHandler {

    private volatile Logger logger;
    private volatile Priority priority = Level.ERROR;

    public Log4JExceptionHandler() {
        this(LogManager.getLogger(Log4JExceptionHandler.class));
    }

    public Log4JExceptionHandler(Logger logger) {
        if (logger == null) throw new NullPointerException();
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        if (logger == null) throw new NullPointerException();
        this.logger = logger;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        if(priority == null)throw new NullPointerException();
        this.priority = priority;
    }

    public void handle(Exception ex) {
        if (!logger.isEnabledFor(priority))
            return;
        logger.log(priority, ex.getMessage(), ex);
    }
}
