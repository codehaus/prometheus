package org.codehaus.prometheus.processors;

/**
 * A Process that receives an item and processes it. It can't return
 * an item. An example of a SinkProcess is a process that writes information
 * to file.
 *
 * @author Peter Veentjer.
 */
public interface SinkProcess<E> extends Process {

    void receive(E msg) throws Exception;
}
