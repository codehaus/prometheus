package org.codehaus.prometheus.monitoring;

import java.util.Map;

public interface Monitorable {
    boolean isOn();

    void turnOn();

    void turnOff();

    void reset();

    Map<String, Object> snapshot();
}
