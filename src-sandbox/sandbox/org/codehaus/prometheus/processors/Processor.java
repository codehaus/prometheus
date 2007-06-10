package org.codehaus.prometheus.processors;

/**
 *
 * @author Peter Veentjer.
 */
public interface Processor<P> {

    /**
     * Returns the process this Processor is executing
     *
     * @return the process this Processor is executing
     */
    P getProcess();

    boolean once()throws Exception;
}
