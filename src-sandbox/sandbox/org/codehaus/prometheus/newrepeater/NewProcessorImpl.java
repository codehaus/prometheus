package org.codehaus.prometheus.newrepeater;

import org.codehaus.prometheus.channels.OutputChannel;
import org.codehaus.prometheus.channels.InputChannel;

public class NewProcessorImpl implements NewProcessor{
    private final InputChannel in;
    private final OutputChannel out;
    private Object[] processes;

    public NewProcessorImpl(InputChannel in, OutputChannel out, Object[] processes){
        this.in = in;
        this.out = out;
        this.processes = processes;
    }

    public void run(RepeatValue repeatValue) throws InterruptedException {
        Object item = in.take();
        for(Object process: processes){
            if(repeatValue.isRunning()){
                item = runProcess(process,item);
            } else{

            }
        }
        out.put(item);
    }

    private Object runProcess(Object process, Object item) {        
        return null;
    }
}
