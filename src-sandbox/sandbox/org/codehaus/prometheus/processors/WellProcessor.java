package org.codehaus.prometheus.processors;

import org.codehaus.prometheus.channels.InputChannel;
import org.codehaus.prometheus.channels.NullInputChannel;
import org.codehaus.prometheus.channels.OutputChannel;
import org.codehaus.prometheus.channels.NullOutputChannel;

public class WellProcessor<E> implements Processor{

    private final WellProcess<E> process;
    private final OutputChannel outputChannel;
    private volatile InputChannel<Event> incomingEventChannel = new NullInputChannel();
    private volatile OutputChannel<Event> outgoingEventChannel = new NullOutputChannel();

    public WellProcessor(WellProcess<E> process, OutputChannel outputChannel) {
        this.process = process;
        this.outputChannel = outputChannel;
    }

    public WellProcess<E> getProcess() {
        return process;
    }

    public InputChannel<Event> getIncomingEventChannel() {
        return incomingEventChannel;
    }

    public void setIncomingEventChannel(InputChannel<Event> incomingEventChannel) {
        this.incomingEventChannel = incomingEventChannel;
    }

    public OutputChannel<Event> getOutgoingEventChannel() {
        return outgoingEventChannel;
    }

    public void setOutgoingEventChannel(OutputChannel<Event> outgoingEventChannel) {
        this.outgoingEventChannel = outgoingEventChannel;
    }

    public OutputChannel getOutputChannel() {
        return outputChannel;
    }

    public boolean processOneMsg() throws Exception {
        Event event = incomingEventChannel.poll();
        if(event == null){
            E msg = process.process();
            outputChannel.put(msg);
        }else{
           // EventDispatcher.dispatch(process,event);
        }
        return true;
    }
}
