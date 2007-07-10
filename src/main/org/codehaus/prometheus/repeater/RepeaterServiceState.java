/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

/**
 * An enumeration of states a {@link RepeaterService} can be in.
 *
 * @author Peter Veentjer.
 */
public enum RepeaterServiceState {
    unstarted, running, shuttingdown, shutdown
}
