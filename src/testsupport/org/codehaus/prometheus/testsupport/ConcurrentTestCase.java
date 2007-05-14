/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import junit.framework.TestCase;

import java.util.concurrent.TimeUnit;
import java.util.*;

/**
 * A TestCase that contains various concurrency related functions.
 *
 * @author Peter Veentjer.
 */
public abstract class ConcurrentTestCase extends TestCase {

    public static final boolean START_INTERRUPTED = true;
    public static final boolean START_UNINTERRUPTED = false;

    public static long DELAY_TINY_MS = 50;
    public static long DELAY_SMALL_MS = DELAY_TINY_MS * 5;//250 ms
    public static long DELAY_MEDIUM_MS = DELAY_TINY_MS * 10;//500 ms
    public static long DELAY_LONG_MS = DELAY_TINY_MS * 50;//2500 msec
    public static long DELAY_EON_MS = 100000000000L;

    public volatile Stopwatch stopwatch;

    //in witch state are threads when they shut down? In some cases they are in blocking mode
    //and there is maybe no 'reason' to bring them out that state. So a thread is allowed to
    //be in the blocking mode, this is not erronous. so when to complain (assertion failure)
    //when to teardown this test.
    public final Set<Thread> threadList = Collections.synchronizedSet(new HashSet<Thread>());

    public ConcurrentTestCase() {
    }

    public ConcurrentTestCase(String fixture) {
        super(fixture);
    }

    public void registerThread(Thread t) {
        if (t == null) throw new NullPointerException();
        threadList.add(t);
    }

    public void giveOthersAChance() {
        giveOthersAChance(DELAY_TINY_MS);
    }

    public void giveOthersAChance(long ms) {
        sleepMs(ms);
        Thread.yield();//operation increases the chance of context switches, but it is allowed to be seen as a no-op
    }

    @Override
    public void setUp() throws Exception {
        stopwatch = new Stopwatch();
    }

    @Override
    public void tearDown() throws Exception {
        threadList.clear();
    }

    /**
     * Joins on all threads. If join times out, the testcase fails, so this call
     * won't block indefinetely, so it is also a way to detect deadlocks (waiting
     * on an event that never occurs).
     * <p/>
     * This method should only be called from the thread that executes this
     * testcase.
     *
     * @param threads the threads to join on.
     */
    public void joinAll(Thread... threads) {
        joinAll(2 * DELAY_LONG_MS,threads);
    }

    public void joinAll(long delayMs, Thread... threads) {
        for (int k = 0; k < threads.length; k++) {
            Thread t = threads[k];
            try {
                long startNs = System.nanoTime();
                t.join(delayMs);
                long endNs = System.nanoTime();
                delayMs -= TimeUnit.NANOSECONDS.toMillis(endNs - startNs);

                if (t.isAlive()) {
                    fail(String.format("thread #%s is still running", k));
                }
            } catch (InterruptedException e) {
                fail(String.format("join on thread #%s was interrupted", k));
            }
        }
    }

    /**
     * @param delay
     * @param unit
     * @param threads
     */
    public void joinAllAndSleep(long delay, TimeUnit unit, Thread... threads) {
        joinAll(threads);
        sleep(delay, unit);
    }

    /**
     * Joins on all threads and sleeps for a cetain amount of time. For more
     * information see {@link #joinAll(Thread[])} and {@link #sleepMs(long)}
     *
     * @param delayMs the number of milliseconds to giveOthersAChance.
     * @param threads the threads to join on.
     */
    public void joinAllAndSleepMs(long delayMs, Thread... threads) {
        joinAllAndSleep(delayMs, TimeUnit.MILLISECONDS, threads);
    }

    /**
     * Sleeps a certain number of milliseconds. If the calling thread is interrupted,
     * the test fails.
     * <p/>
     * Only the thread that runs the testcase should call this method.
     *
     * @param ms the number of milliseconds to giveOthersAChance.
     */
    public void sleepMs(long ms) {
        sleep(ms, TimeUnit.MILLISECONDS);
    }

    /**
     * Sleeps a certain amount of time. If the calling thread is interrupted, the test
     * fails.
     * <p/>
     * Only the thread that runs this testcase should call this method.
     *
     * @param period the period to giveOthersAChance. If the number is equal or smaller than zero, no
     *               sleeping is done.
     * @param unit   the timeunit of period
     * @throws NullPointerException if unit is null.
     */
    public void sleep(long period, TimeUnit unit) {
        if (unit == null) throw new NullPointerException();
        if (period <= 0)
            return;

        long periodNs = unit.toNanos(period);
        long ms = TimeUnit.NANOSECONDS.toMillis(periodNs);
        int ns = (int) (periodNs % TimeUnit.MILLISECONDS.toNanos(1));

        try {
            Thread.sleep(ms, ns);
        } catch (InterruptedException e) {
            fail("giveOthersAChance was interrupted");
        }
    }
}

