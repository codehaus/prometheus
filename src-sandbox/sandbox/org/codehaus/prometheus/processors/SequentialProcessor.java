package org.codehaus.prometheus.processors;

import org.codehaus.prometheus.channels.InputChannel;
import org.codehaus.prometheus.channels.OutputChannel;

/**
 * todo:
 * this class can be integrated in the {@link org.codehaus.prometheus.processors.standardprocessor.StandardProcessor}.
 */
public class SequentialProcessor implements Processor{

    private final InputChannel input;
    private final OutputChannel output;
    private final Object[] processes;
    private volatile Dispatcher dispatcher = new StandardDispatcher();

    public SequentialProcessor(InputChannel input, Object[]  processes, OutputChannel output) {
        this.input = input;
        this.output = output;
        this.processes = processes;
    }

    public boolean once() throws Exception {
        Object in;
        Object out;

        if(input == null){

        }else{

        }

        return true;
    }
}
