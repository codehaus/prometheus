package org.codehaus.prometheus.monitoring;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A MonitoringRunnable should be
 */
public class MonitoringRunnable implements Runnable, Monitorable {

    private final AtomicLong count = new AtomicLong();
    private final AtomicLong lastExecution = new AtomicLong();
    private volatile boolean on = false;

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
        Map<String, Object> snapshot = new HashMap();
        return snapshot;
    }

    protected void monitoredRun() {

    }

    public final void run() {
        if (on) {
            monitoredRun();
        } else {

        }
    }
}
