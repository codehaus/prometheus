package org.codehaus.prometheus.processors;

public interface ProcessEventDispatcher {

    boolean dispatch(Process process, ProcessorEvent e);
}
