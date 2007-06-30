package org.codehaus.prometheus.resequencer;

/**
 * @author Peter Veentjer.
 */
public interface Resequencer<S, E> {

    void put(S sequenceId, E item) throws InterruptedException;

    S nextSequenceId();
}
