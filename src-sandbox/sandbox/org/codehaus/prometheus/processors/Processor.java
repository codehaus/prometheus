package org.codehaus.prometheus.processors;

/**
 * @author Peter Veentjer.
 */
public interface Processor<P> {

    /**
     * Run the Processor once.
     *
     * @return true if it should run again, false otherwise.
     * @throws Exception
     */
    boolean once() throws Exception;
}
