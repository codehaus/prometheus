/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import java.util.concurrent.TimeUnit;

/**
 * The TimedRepeatingTestThread is a support thread, that makes it easy to keep repeating a task
 * for a certain amount of time.
 *
 * If the maximum running time is exceeded, the thread is not interrupted (maybe feature for the
 * future?)
 *
 * @author Peter Veentjer.
 */
///CLOVER:OFF
public class TimedRepeatingThread extends TestThread {
    private final long maxTimeNs;
    private final Runnable task;

    public TimedRepeatingThread(long maxtime, TimeUnit unit, Runnable task) {
        this.maxTimeNs = unit.toNanos(maxtime);
        this.task = task;
    }

    @Override
    public void runInternal() {
        long startTimeNs = System.nanoTime();
        do {
            task.run();
        } while (System.nanoTime() - startTimeNs < maxTimeNs);
    }    
}

