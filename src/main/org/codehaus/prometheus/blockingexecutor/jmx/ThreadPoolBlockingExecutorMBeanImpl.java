/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor.jmx;

import org.codehaus.prometheus.blockingexecutor.BlockingExecutorServiceState;
import org.codehaus.prometheus.blockingexecutor.ThreadPoolBlockingExecutor;

/**
 * Default implementation of the {@link ThreadPoolBlockingExecutorMBean}.
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public class ThreadPoolBlockingExecutorMBeanImpl implements ThreadPoolBlockingExecutorMBean {

    private final ThreadPoolBlockingExecutor executor;

    /**
     * Constructs a new ThreadPoolBlockingExecutorMBeanImpl.
     *
     * @param executor the ThreadPoolBlockingExecutorMBeanImpl this MBean exposes.
     * @throws NullPointerException if executor is null.
     */
    public ThreadPoolBlockingExecutorMBeanImpl(ThreadPoolBlockingExecutor executor) {
        if (executor == null) throw new NullPointerException();
        this.executor = executor;
    }

    public ThreadPoolBlockingExecutor getExecutor() {
        return executor;
    }

    public void shutdownNow() {
        executor.shutdownNow();
    }

    public void shutdown() {
        executor.shutdownPolitly();
    }

    public int getActualPoolSize() {
        return executor.getActualPoolSize();
    }

    public int getDesiredPoolSize() {
        return executor.getDesiredPoolSize();
    }

    public void setDesiredPoolSize(int poolsize) {
        executor.setDesiredPoolSize(poolsize);
    }

    public void start() {
        executor.start();
    }

    public BlockingExecutorServiceState getState() {
        return executor.getState();
    }
}
