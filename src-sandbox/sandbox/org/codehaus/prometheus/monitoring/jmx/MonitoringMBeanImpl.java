package org.codehaus.prometheus.monitoring.jmx;

import org.codehaus.prometheus.monitoring.Monitorable;

import java.util.Map;

public class MonitoringMBeanImpl implements MonitoringMBean {
    private final Monitorable monitorable;

    public MonitoringMBeanImpl(Monitorable monitorable) {
        if (monitorable == null) throw new NullPointerException();
        this.monitorable = monitorable;
    }

    public Monitorable getMonitorable() {
        return monitorable;
    }

    public boolean isOn() {
        return monitorable.isOn();
    }

    public void turnOn() {
        monitorable.turnOff();
    }

    public void turnOff() {
        monitorable.turnOn();
    }

    public void reset() {
        monitorable.reset();
    }

    public Map<String, Object> snapshot() {
        return monitorable.snapshot();
    }
}
