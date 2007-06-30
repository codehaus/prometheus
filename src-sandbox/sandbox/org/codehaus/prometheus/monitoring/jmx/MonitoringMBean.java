package org.codehaus.prometheus.monitoring.jmx;

import java.util.Map;

public interface MonitoringMBean {
    boolean isOn();

    void turnOn();

    void turnOff();

    void reset();

    Map<String, Object> snapshot();
}
