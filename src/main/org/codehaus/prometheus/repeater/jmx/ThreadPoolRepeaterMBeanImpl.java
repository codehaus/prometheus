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
 */
public class ThreadPoolRepeaterMBeanImpl implements ThreadPoolRepeaterMBean {

    private final ThreadPoolRepeater repeater;

    /**
     * Constructs a new RepeaterServiceMBean
     *
     * @param threadPoolRepeater the RepeaterService that is exposed by this MBean.
     * @throws NullPointerException if repeater is <tt>null</tt>.
     */
    public ThreadPoolRepeaterMBeanImpl(ThreadPoolRepeater threadPoolRepeater){
        if(threadPoolRepeater == null)throw new NullPointerException();
        this.repeater = threadPoolRepeater;
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
        repeater.shutdownNow();
    }

    public void start(){
        repeater.start();
    }

    public RepeaterServiceState getState() {
        return repeater.getState();
    }

    public int getActualPoolSize() {
        return repeater.getActualPoolSize();
    }

    public int getPoolSize() {
        return repeater.getPoolSize();
    }

    public void setPoolSize(int poolsize) {
        repeater.setPoolSize(poolsize);
    }
}
