/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater.jmx;

import org.codehaus.prometheus.repeater.RepeaterServiceState;
import org.codehaus.prometheus.repeater.ThreadPoolRepeater;

/**
 * The default implementation of the {@link ThreadPoolRepeaterMBean} interface.
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public class ThreadPoolRepeaterMBeanImpl implements ThreadPoolRepeaterMBean {

    private final ThreadPoolRepeater repeater;

    /**
     * Constructs a new RepeaterServiceMBean
     *
     * @param repeater the RepeaterService that is exposed by this MBean.
     * @throws NullPointerException if repeater is <tt>null</tt>.
     */
    public ThreadPoolRepeaterMBeanImpl(ThreadPoolRepeater repeater) {
        if (repeater == null) throw new NullPointerException();
        this.repeater = repeater;
    }

    /**
     * Returns the ThreadPoolRepeater this MBean exposes.
     *
     * @return the ThreadPoolRepeater this MBean exposes.
     */
    public ThreadPoolRepeater getRepeater() {
        return repeater;
    }

    public void shutdown() {
        repeater.shutdownPolitly();
    }

    public void shutdownNow() {
        repeater.shutdownNow();
    }

    public void start() {
        repeater.start();
    }

    public RepeaterServiceState getState() {
        return repeater.getState();
    }

    public int getActualPoolSize() {
        return repeater.getActualPoolSize();
    }

    public int getDesiredPoolSize() {
        return repeater.getDesiredPoolSize();
    }

    public void setDesiredPoolSize(int poolsize) {
        repeater.setDesiredPoolSize(poolsize);
    }
}
