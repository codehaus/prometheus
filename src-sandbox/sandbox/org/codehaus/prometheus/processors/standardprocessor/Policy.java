package org.codehaus.prometheus.processors.standardprocessor;

/**
 * The Policy is responsible for dealing with exception that occurred while executing
 * a process in the StandardProcessor.
 *
 * It is only used for exceptions, not for errors and other throwables that are not exceptions.
 *
 * @author Peter Veentjer.
 */
public interface Policy {
    Object handle(Exception ex, Object... in) throws Exception;
}

