package org.codehaus.prometheus.processors;

import org.codehaus.prometheus.monitoring.Monitorable;

import java.util.Map;

public class MonitoringProcessor implements Processor, Monitorable {
    private final Processor target;
    private volatile boolean on = false;

    public MonitoringProcessor(Processor target){
        this.target = target;
    }

    public Processor getTarget() {
        return target;
    }

    public Object getProcess() {
        return target.getProcess();
    }

    public boolean once() throws Exception {
        if(on){
            return target.once();
        }else{
            return target.once();
        }
    }

    public boolean isOn() {
        return on;
    }

    public void turnOn() {
        on = true;
    }

    public void turnOff() {
        on = false;
    }

    public void reset() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, Object> snapshot() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
