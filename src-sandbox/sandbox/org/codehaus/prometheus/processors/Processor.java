package org.codehaus.prometheus.processors;

/**
 * @author Peter Veentjer.
 */
public interface Processor {

    /**
     * Run the Processor runOnce.
     *
     * @return true if it should run again, false otherwise.
     * @throws Exception
     */
    boolean runOnce() throws Exception;
}
