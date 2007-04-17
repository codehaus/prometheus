/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor.jmx;

import org.codehaus.prometheus.blockingexecutor.BlockingExecutorServiceState;

/**
 * The BlockingExecutorServiceMBean is an MBean that exposes an
 * {@link org.codehaus.prometheus.blockingexecutor.BlockingExecutorService}.
 *
 * @author Peter Veentjer.
 */
public interface BlockingExecutorServiceMBean {

    /**
     * For more information see {@link org.codehaus.prometheus.blockingexecutor.BlockingExecutorService#start()}
     */
    void start();

    /**
     * For more information see {@link org.codehaus.prometheus.blockingexecutor.BlockingExecutorService#shutdown()}
     */
    void stop();

    /**
     * For more information see {@link org.codehaus.prometheus.blockingexecutor.BlockingExecutorService#getState()}
     */
    BlockingExecutorServiceState getState();
}
