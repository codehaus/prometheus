package org.codehaus.prometheus.processors.standardprocessor;

import org.codehaus.prometheus.processors.ProcessDeath;

public class StandardStopStrategy implements StopStrategy{
    public boolean stop(Object item) {
        return item instanceof ProcessDeath;
    }
}
