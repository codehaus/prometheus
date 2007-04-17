/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor.jmx;

import org.codehaus.prometheus.blockingexecutor.BlockingExecutorService;
import org.codehaus.prometheus.blockingexecutor.BlockingExecutorServiceState;

/**
 * The BlockingExecutorServiceMBeanImpl is the default implementation of the
 * {@link BlockingExecutorServiceMBean}.
 *
 * @author Peter Veentjer.
 */
public class BlockingExecutorServiceMBeanImpl implements BlockingExecutorServiceMBean {

    private final BlockingExecutorService executorService;

    /**
     * Constructs a new BlockingExecutorServiceMBean.
     *
     * @param executorService the BlockingExecutorService this MBean exposes.
     * @throws NullPointerException if executorService is null.
     */
    public BlockingExecutorServiceMBeanImpl(BlockingExecutorService executorService) {
        if (executorService == null) throw new NullPointerException();
        this.executorService = executorService;
    }

    public void start() {
        executorService.start();
    }

    public void stop() {
        executorService.shutdown();
    }

    public BlockingExecutorServiceState getState() {
        return executorService.getState();
    }
}
