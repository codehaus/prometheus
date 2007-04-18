package org.codehaus.prometheus.threadpool;

/**
 * The Status a {@link ThreadPool} can be in.
 *
 * @author Peter Veentjer.
 */
public enum ThreadPoolState {
    unstarted, started, shuttingdown, shutdown
}
