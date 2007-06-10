package org.codehaus.prometheus.processors;

import org.codehaus.prometheus.repeater.Repeatable;

/**
 * A {@link Repeatable} that repeats a {@link Processor}.
 */
public class ProcessorRepeatable implements Repeatable {

    private final Processor processor;

    public ProcessorRepeatable(Processor processor) {
        if (processor == null) throw new NullPointerException();
        this.processor = processor;
    }

    public Processor getProcessor() {
        return processor;
    }

    public boolean execute() throws Exception {
        return processor.once();
    }
}
