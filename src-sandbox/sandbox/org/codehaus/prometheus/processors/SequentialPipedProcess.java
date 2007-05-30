package org.codehaus.prometheus.processors;

import java.util.List;

/**
 * Not all processes need to be parallel. Some can be executed sequential.
 */
public class SequentialPipedProcess implements PipedProcess{
    private SourceProcess sourceProcess;
    private SinkProcess sinkProcess;
    private List<PipedProcess> pipedProcesses;

    public Object process(Object msg) throws Exception {
       for(PipedProcess p: pipedProcesses){
           msg = p.process(msg);
       }

        return msg;
    }
}
