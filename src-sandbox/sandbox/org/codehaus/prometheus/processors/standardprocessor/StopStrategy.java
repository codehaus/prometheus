package org.codehaus.prometheus.processors.standardprocessor;

/**
 * A Policy that provides a way for the StandardProcessor to stop based on some message.
 *
 * @author Peter Veentjer.
 */
public interface StopStrategy {

    boolean stop(Object item);
}
