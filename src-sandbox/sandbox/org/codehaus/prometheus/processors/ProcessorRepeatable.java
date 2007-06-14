package org.codehaus.prometheus.processors;

import org.codehaus.prometheus.repeater.Repeatable;

/**
 * A {@link Repeatable} that repeats a {@link Processor}. This class can be seen
 * as an adapter: it makes it possible to run the process as a repeatable.
 *
 * @author Peter Veentjer.
 */
public class ProcessorRepeatable implements Repeatable {

    private final Processor processor;

    /**
     * Creates a new ProcessorRepeatable that executes the given processor.
     *
     * @param processor the Processor to execute.
     * @throws NullPointerException if processor is null.
     */
    public ProcessorRepeatable(Processor processor) {
        if (processor == null) throw new NullPointerException();
        this.processor = processor;
    }

    public Processor getProcessor() {
        return processor;
    }

    public boolean execute() throws Exception {
        boolean again =  processor.once();
        if(!again){
            //todo: hack
            System.out.println("shutting down: "+processor);
        }
        return again;
    }
}
