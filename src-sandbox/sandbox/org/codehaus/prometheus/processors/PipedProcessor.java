package org.codehaus.prometheus.processors;

import org.codehaus.prometheus.channels.InputChannel;
import org.codehaus.prometheus.channels.NullInputChannel;
import org.codehaus.prometheus.channels.NullOutputChannel;
import org.codehaus.prometheus.channels.OutputChannel;

/**
 * A {@link Processor} that executes a {@link PipedProcess}. The input is taken from an {@link InputChannel}
 * and the output is placed on an {@Link OutputChannel}.
 */
public class PipedProcessor<E, F> implements Processor {
    private final PipedProcess<E, F> process;
    private final InputChannel<E> input;
    private final OutputChannel<F> output;
    private final EventDispatcher eventDispatcher = null;
    private volatile InputChannel<Event> incomingEventChannel = new NullInputChannel<Event>();
    private volatile OutputChannel<Event> outgoingEventChannel = new NullOutputChannel<Event>();
    //todo: sequence stuff.

    public PipedProcessor(PipedProcess<E, F> process, InputChannel<E> input, OutputChannel<F> output) {
        if (process == null) throw new NullPointerException();
        this.process = process;
        this.input = input;
        this.output = output;
    }

    public PipedProcess<E, F> getProcess() {
        return process;
    }

    public InputChannel<E> getInput() {
        return input;
    }

    public OutputChannel<F> getOutput() {
        return output;
    }

    public void setIncomingEventChannel(InputChannel<Event> incomingEventChannel) {
        this.incomingEventChannel = incomingEventChannel;
    }

    public boolean processOneMsg() throws Exception {
        Event event = incomingEventChannel.poll();
        if (event == null) {
            E input = this.input.take();
            F result = process.process(input);
            if (result != null) {
                output.put(result);
            }

        } else {
            eventDispatcher.dispatch(process, event);
        }

        return true;
    }
}
