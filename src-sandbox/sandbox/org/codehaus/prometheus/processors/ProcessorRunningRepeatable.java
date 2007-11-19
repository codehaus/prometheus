package org.codehaus.prometheus.processors;

import org.codehaus.prometheus.repeater.Repeatable;

/**
 * A {@link Repeatable} that repeats a {@link Processor}. This class can be seen
 * as an adapter: it makes it possible to run the process as a repeatable. Every
 * time the {@link Repeatable#execute()} method is called, the {@link Processor#runOnce()} is
 * called. Exceptions thrown by this call are not handled by this ProcessorRunningRepeatable.
 *
 * @author Peter Veentjer.
 */
public class ProcessorRunningRepeatable implements Repeatable {

    private final Processor processor;

    /**
     * Creates a new ProcessorRunningRepeatable that executes the given processor.
     *
     * @param processor the Processor to execute.
     * @throws NullPointerException if processor is null.
     */
    public ProcessorRunningRepeatable(Processor processor) {
        if (processor == null) throw new NullPointerException();
        this.processor = processor;
    }

    /**
     * Returns the Processor that is run by this ProcessorRunningRepeatable.
     *
     * @return the Processor that is run by this ProcessorRunningRepeatable.
     */
    public Processor getProcessor() {
        return processor;
    }

    public boolean execute() throws Exception {
        boolean again = processor.runOnce();
        if (!again) {
            //todo: hack
            System.out.println("shutting down: " + processor);
        }
        return again;
    }
}
