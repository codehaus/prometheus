package org.codehaus.prometheus.processors;

import org.codehaus.prometheus.repeater.Repeatable;
import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;

/**
 * A {@link Repeatable} that repeats a {@link Processor}.
 */
public class ProcessorRunningRepeatable implements Repeatable {

    private final Processor processor;
    private final ExceptionHandler handler;

    public ProcessorRunningRepeatable(Processor processor, ExceptionHandler handler) {
        this.processor = processor;
        this.handler = handler;
    }

    public ExceptionHandler getHandler() {
        return handler;
    }

    public Processor getProcessor() {
        return processor;
    }

    public boolean execute() {
        try {
            return processor.processOneMsg();            
        } catch (Exception e) {
            handler.handle(e);
            return true;
        }
    }
}
