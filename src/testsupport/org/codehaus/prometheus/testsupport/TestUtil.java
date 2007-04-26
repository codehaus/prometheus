/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import org.codehaus.prometheus.util.ConcurrencyUtil;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Most functions in this  utility class don't thrown Interruptedexception. But the internals
 * of the function in some cases have to deal with this exception. I catch this exception,
 * wrap it in a RuntimeException and throw this, and I also set the interrupted flag on the
 * thread.
 *
 * @author Peter Veentjer.
 */
public class TestUtil {

    public static void sleepRandomMs(long maxSleepMs) {
        sleepRandom(maxSleepMs, TimeUnit.MILLISECONDS);
    }

    public static void sleepRandom(long maxSleep, TimeUnit unit) {
        Random r = new Random();
        long sleepNs = Math.abs(r.nextInt()) % maxSleep;

        long ms = unit.toMillis(sleepNs);
        int ns = (int) (unit.toNanos(sleepNs) % (1000 * 1000));
        try {
            Thread.sleep(ms, ns);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * Sleeps a period in miliseconds.
     *
     * @param ms
     */
    public static void sleepMs(long ms) {
        //todo: remove dependency
        ConcurrencyUtil.sleepUninterruptibly(ms, TimeUnit.MILLISECONDS);
    }


    public static void allowOtherThreadsToRun() {
        Thread.yield();
    }

    public static TestThread scheduleSignallAll(final Lock lock, final Condition cond) {
        if (lock == null || cond == null) throw new NullPointerException();

        Runnable notifier = new Runnable() {
            public void run() {
                lock.lock();
                try {
                    cond.signalAll();
                } finally {
                    lock.unlock();
                }
            }
        };

        TestThread t = new TestThread(notifier);
        t.start();
        return t;
    }


    public static TestThread scheduleSpuriousWakeup(final Lock lock, final Condition cond, final long delay) {
        if (lock == null || cond == null) throw new NullPointerException();

        Runnable notifier = new Runnable() {
            public void run() {
                //remove dependency                
                ConcurrencyUtil.sleepUninterruptibly(delay, TimeUnit.MILLISECONDS);

                lock.lock();
                try {
                    cond.signalAll();
                } finally {
                    lock.unlock();
                }
            }
        };

        TestThread t = new TestThread(notifier);
        t.start();
        return t;
    }

    public static void someCalculation(long iterations) {
        double pi = PI(iterations);
        if (pi <= 0)
            System.out.println("Benchmark exited with unrealistic value " + pi);
    }

    public static double PI(long i) {
        double total = 0.0;
        for (long j = 1; j <= i; j += 4)
            total += 1.0 / j - 1.0 / (j + 2);
        return 4 * total;
    }
}
