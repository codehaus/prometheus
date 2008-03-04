package org.codehaus.prometheus.newrepeater;

import org.codehaus.prometheus.repeater.Repeatable;

public class NewProcessorRepeatable implements Repeatable {

    private final NewProcessor newProcessor;

    public NewProcessorRepeatable(NewProcessor newProcessor){
        this.newProcessor = newProcessor;
    }

    public boolean execute() throws Exception {
        newProcessor.run(null);
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
