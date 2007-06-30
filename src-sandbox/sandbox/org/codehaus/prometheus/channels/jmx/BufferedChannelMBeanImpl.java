package org.codehaus.prometheus.channels.jmx;

import org.codehaus.prometheus.channels.BufferedChannel;

/**
 * @author Peter Veentjer.
 */
public class BufferedChannelMBeanImpl implements BufferedChannelMBean {
    private final BufferedChannel target;

    public BufferedChannelMBeanImpl(BufferedChannel target) {
        if (target == null) throw new NullPointerException();
        this.target = target;
    }

    public BufferedChannel getTarget() {
        return target;
    }

    public int size() {
        return target.size();
    }

    public void setRemainingCapacity(int capacity) {
        target.setRemainingCapacity(capacity);
    }

    public int getRemainingCapacity() {
        return target.getRemainingCapacity();
    }
}
