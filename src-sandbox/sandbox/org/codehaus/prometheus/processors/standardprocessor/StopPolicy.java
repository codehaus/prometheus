package org.codehaus.prometheus.processors.standardprocessor;

/**
 * A Policy that provides a way for the StandardProcessor to shouldStop based on some message.
 *
 * @author Peter Veentjer.
 */
public interface StopPolicy {

    /**
     * @param item the item to check
     * @return true if the item indicates stop.
     * @throws NullPointerException if item is null. It is up the the implementation
     *                              to decide if the check needs to be done, but the
     *                              StandardProcessor will never call this with a null
     *                              value.
     */
    boolean shouldStop(Object item);
}
