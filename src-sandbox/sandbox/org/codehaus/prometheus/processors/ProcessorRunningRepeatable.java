package org.codehaus.prometheus.processors;

import org.codehaus.prometheus.repeater.Repeatable;
import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.exceptionhandler.Log4JExceptionHandler;

/**
 * A {@link Repeatable} that repeats a {@link Processor}.
 */
public class ProcessorRunningRepeatable implements Repeatable {

    private final Processor processor;
    private final ExceptionHandler handler;

    public ProcessorRunningRepeatable(Processor processor){
        this(processor,new Log4JExceptionHandler());
    }

    public ProcessorRunningRepeatable(Processor processor, ExceptionHandler handler) {
        if(processor == null||handler == null)throw new NullPointerException();
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
