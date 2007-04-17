/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import junit.framework.TestCase;

import java.util.concurrent.TimeUnit;

/**
 * Improvements:
 * elapsed time should be returned, even though the stopwatch hasn't stopped.
 * stopwatch shouldn't be stopped multiple times.
 *
 * @author Peter Veentjer.
 */
public class Stopwatch {

    private volatile long beginNs;
    private volatile long endNs;
    private volatile boolean stopped = false;

    public Stopwatch(){}

    public long getBeginNs() {
        return beginNs;
    }

    public long getEndNs() {
        return endNs;
    }

    public void start() {
        beginNs = System.nanoTime();
    }

    public void stop() {
        endNs = System.nanoTime();
    }

    public long calcElapsedMs() {
        long elapsedNs = endNs - beginNs;
        return TimeUnit.NANOSECONDS.toMillis(elapsedNs);
    }

    public void assertElapsedSmallerThanMs(long ms) {
        long elapsedMs = calcElapsedMs();
        String msg = "elapsed:"+elapsedMs+" ms:"+ms;
        TestCase.assertTrue(msg,elapsedMs < ms);
    }

    public void assertElapsedBiggerOrEqualThanMs(long ms) {
        long elapsedMs = calcElapsedMs();
        TestCase.assertTrue(elapsedMs >= ms);
    }
}
