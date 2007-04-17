package org.codehaus.prometheus.processors;

/**
 * A PipedProcess
 *
 * Important: the process should not contain logic about threading, where the msg is taken from
 * and where the result message is taken to.
 */
public interface PipedProcess<E,F> extends Process{
    
    F process(E msg)throws Exception;
        
}
