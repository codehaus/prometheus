package org.codehaus.prometheus.processors;

/**
 * A Process that receives a msg, processes it and returns a message (could be the same one but
 * could also be a different one).
 *
 * Important: the process should not contain logic about threading, where the msg is taken from
 * and where the result message is taken to.
 *
 * @author Peter Veentjer.
 */
public interface PipedProcess<E,F> extends Process{
    
    F process(E msg)throws Exception;
        
}
