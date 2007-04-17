package org.codehaus.prometheus.processors;

import org.codehaus.prometheus.channels.InputChannel;

public class SinkProcessor<E> implements Processor{

    private final SinkProcess<E> process;
    private final InputChannel<E> input;

    public SinkProcessor(SinkProcess<E> process, InputChannel<E> input) {
        this.process = process;
        this.input = input;
    }

    public SinkProcess getProcess() {
        return process;
    }

    public boolean processOneMsg() throws Exception {
        E msg = input.take();
        process.receive(msg);
        return true;
    }
}
