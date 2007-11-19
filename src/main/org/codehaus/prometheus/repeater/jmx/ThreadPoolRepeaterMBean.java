/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater.jmx;

import org.codehaus.prometheus.repeater.RepeaterServiceState;

/**
 * An MBean that exposes a {@link org.codehaus.prometheus.repeater.ThreadPoolRepeater}.
 * <p/>
 * The RepeaterServiceMBean doesn't expose any of the repeat methods because I haven't seen a good
 * use-case where a task is entered by JMX. If this functionality is required, you have to write a
 * MBean yourself.
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public interface ThreadPoolRepeaterMBean {

    /**
     * See {@link org.codehaus.prometheus.repeater.ThreadPoolRepeater#shutdownPolitly()}
     */
    void shutdown();

    /**
     * See {@link org.codehaus.prometheus.repeater.ThreadPoolRepeater#shutdownNow()}
     */
    void shutdownNow();

    /**
     * see {@link org.codehaus.prometheus.repeater.ThreadPoolRepeater#start()}
     */
    void start();

    /**
     * See {@link org.codehaus.prometheus.repeater.ThreadPoolRepeater#getState()}.
     *
     * @return the state.
     */
    RepeaterServiceState getState();

    /**
     * See {@link org.codehaus.prometheus.repeater.ThreadPoolRepeater#getDesiredPoolSize()}
     *
     * @return the poolsize.
     */
    int getDesiredPoolSize();

    /**
     * See {@link org.codehaus.prometheus.repeater.ThreadPoolRepeater#setDesiredPoolSize(int)}
     *
     * @param poolsize the new poolsize
     * @throws IllegalArgumentException if poolsize is smaller than 0.
     */
    void setDesiredPoolSize(int poolsize);

    /**
     * See {@link org.codehaus.prometheus.repeater.ThreadPoolRepeater#getActualPoolSize()}.
     *
     * @return the actual poolsize.
     */
    int getActualPoolSize();
}
