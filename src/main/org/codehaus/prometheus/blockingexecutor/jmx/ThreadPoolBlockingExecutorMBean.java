/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor.jmx;

import org.codehaus.prometheus.blockingexecutor.BlockingExecutorServiceState;

/**
 * A MBean that exposes a {@link org.codehaus.prometheus.blockingexecutor.ThreadPoolBlockingExecutor}
 *
 * @author Peter Veentjer.
 */
public interface ThreadPoolBlockingExecutorMBean {

    /**
     * Starts the {@link org.codehaus.prometheus.blockingexecutor.ThreadPoolBlockingExecutor}.
     * <p/>
     * For more information see {@link org.codehaus.prometheus.blockingexecutor.ThreadPoolBlockingExecutor#start()}
     */
    void start();

    /**
     * For more information see
     * {@link org.codehaus.prometheus.blockingexecutor.ThreadPoolBlockingExecutor#shutdown()}
     */
    void shutdown();

    /**
     * For more information see
     * {@link org.codehaus.prometheus.blockingexecutor.ThreadPoolBlockingExecutor#shutdownNow()}.
     * The reason that the unexecuted tasks are not returned, it that they don't need to be serializable.
     */
    void shutdownNow();

    /**
     * For more information see
     * {@link org.codehaus.prometheus.blockingexecutor.ThreadPoolBlockingExecutor#getActualPoolSize()}
     *
     * @return the actual number of threads in the pool.
     */
    int getActualPoolSize();

    /**
     * @return the desired poolsize.
     */
    int getDesiredPoolSize();

    /**
     * For more information see
     * {@link org.codehaus.prometheus.blockingexecutor.ThreadPoolBlockingExecutor#setDesiredPoolSize(int)}
     *
     * @param poolsize
     */
    void setDesiredPoolSize(int poolsize);

    /**
     * For more information see
     * {@link org.codehaus.prometheus.blockingexecutor.ThreadPoolBlockingExecutor#getState()}
     */
    BlockingExecutorServiceState getState();
}
