/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.concurrenttesting;

/**
 * The state a {@link BlockingRunnable} or a {@link TestThread} can be in.
 *
 * @author Peter Veentjer.
 */
public enum BlockingState {
    waiting, finished, timeout, interrupted
}
