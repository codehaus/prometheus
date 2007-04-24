package org.codehaus.prometheus.processors;

import org.codehaus.prometheus.channels.InputChannel;
import org.codehaus.prometheus.channels.NullInputChannel;
import org.codehaus.prometheus.channels.OutputChannel;
import org.codehaus.prometheus.channels.NullOutputChannel;

public class WellProcessor<E> implements Processor{

    private final WellProcess<E> process;
    private final OutputChannel outputChannel;
    private volatile InputChannel<ProcessorEvent> incomingEventChannel = new NullInputChannel();
    private volatile OutputChannel<ProcessorEvent> outgoingEventChannel = new NullOutputChannel();

    public WellProcessor(WellProcess<E> process, OutputChannel outputChannel) {
        this.process = process;
        this.outputChannel = outputChannel;
    }

    public WellProcess<E> getProcess() {
        return process;
    }

    public InputChannel<ProcessorEvent> getIncomingEventChannel() {
        return incomingEventChannel;
    }

    public void setIncomingEventChannel(InputChannel<ProcessorEvent> incomingEventChannel) {
        this.incomingEventChannel = incomingEventChannel;
    }

    public OutputChannel<ProcessorEvent> getOutgoingEventChannel() {
        return outgoingEventChannel;
    }

    public void setOutgoingEventChannel(OutputChannel<ProcessorEvent> outgoingEventChannel) {
        this.outgoingEventChannel = outgoingEventChannel;
    }

    public OutputChannel getOutputChannel() {
        return outputChannel;
    }

    public boolean processOneMsg() throws Exception {
        ProcessorEvent event = incomingEventChannel.poll();
        if(event == null){
            E msg = process.process();
            outputChannel.put(msg);
        }else{
           // ProcessEventDispatcher.dispatch(process,event);
        }
        return true;
    }
}
