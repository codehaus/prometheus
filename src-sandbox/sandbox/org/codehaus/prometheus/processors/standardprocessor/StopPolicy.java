package org.codehaus.prometheus.processors.standardprocessor;

/**
 * A Policy that provides a way for the {@link StandardProcessor} to stop based on some message.
 *
 * @author Peter Veentjer.
 */
public interface StopPolicy {

    /**
     * Checks if the Processor should stop based on some message.
     *
     * @param message the message to check
     * @return true if the item indicates stop.
     * @throws NullPointerException if message is null. It is up the the implementation
     *                              to decide if the check needs to be done, but the
     *                              StandardProcessor will never call this with a null
     *                              value.
     */
    boolean shouldStop(Object message);
}
