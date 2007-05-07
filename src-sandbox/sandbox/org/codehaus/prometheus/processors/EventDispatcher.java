package org.codehaus.prometheus.processors;

public interface EventDispatcher {

    /**
     *
     * @param process
     * @param e
     * @return true if the dispatch was successful, false otherwise.
     */
    boolean dispatch(Process process, Event e) throws Exception;
}
