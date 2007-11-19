/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

/**
 * A ThreadPoolJob specific for testing purposes.
 *
 * @author Peter Veentjer.
 */
public class DummyThreadPoolJob implements ThreadPoolJob {

    public Object takeWork() throws InterruptedException {
        throw new RuntimeException();
    }

    public Object takeWorkForNormalShutdown() throws InterruptedException {
        throw new RuntimeException();
    }

    public boolean executeWork(Object task) throws Exception {
        throw new RuntimeException();
    }
}
