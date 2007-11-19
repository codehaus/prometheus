package org.codehaus.prometheus.processors;

import org.codehaus.prometheus.monitoring.Monitorable;

import java.util.Map;

/**
 * A Processor that decorates another processor and monitors its behaviour.
 *
 * @author Peter Veentjer.
 */
public class MonitoringProcessor implements Processor, Monitorable {
    private final Processor target;
    private volatile boolean on = false;

    public MonitoringProcessor(Processor target) {
        if (target == null) throw new NullPointerException();
        this.target = target;
    }

    public Processor getTarget() {
        return target;
    }

    public boolean runOnce() throws Exception {
        if (on) {
            //todo: add monitor data.
            return target.runOnce();
        } else {
            return target.runOnce();
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
