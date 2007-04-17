/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

/**
 * An enumeration of states a {@link BlockingExecutorServiceState} can be in.
 *
 * @author Peter Veentjer.
 */
public enum BlockingExecutorServiceState {
    Unstarted, Running, Shuttingdown, Shutdown
}
