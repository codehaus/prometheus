package org.codehaus.prometheus.processors;

/**
 * A SinkProcess is a Process that takes an input message, without returning anyting.
 */
public interface SinkProcess<E> extends Process {

    void receive(E msg) throws Exception;
}
