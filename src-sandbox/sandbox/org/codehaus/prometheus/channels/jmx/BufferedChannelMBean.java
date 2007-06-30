package org.codehaus.prometheus.channels.jmx;

/**
 * @author Peter Veentjer.
 */
public interface BufferedChannelMBean {

    int size();

    void setRemainingCapacity(int capacity);

    int getRemainingCapacity();
}
