package org.codehaus.prometheus.processors.standardprocessor;

/**
 * The ErrorPolicy is responsible for dealing with exception that occurred while executing
 * a process in the StandardProcessor.
 * <p/>
 * It is only used for exceptions, not for errors and other throwables that are not exceptions.
 *
 * @author Peter Veentjer.
 */
public interface ErrorPolicy {

    /**
     * @param ex
     * @param in
     * @return
     * @throws Exception
     */
    Object handleReceiveError(Exception ex, Object in) throws Exception;
}

