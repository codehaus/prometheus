/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

/**
 * The Status a {@link ThreadPool} can be in.
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public enum ThreadPoolState {
    unstarted, running, shuttingdown, forcedshuttingdown, shutdown
}
