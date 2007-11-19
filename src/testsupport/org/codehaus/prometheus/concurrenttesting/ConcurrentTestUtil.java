/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.concurrenttesting;

import junit.framework.Assert;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.codehaus.prometheus.util.ConcurrencyUtil.sleepUninterruptibly;

import static java.lang.String.format;
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
public final class ConcurrentTestUtil {

    //random is not threadsafe, so make sure proper synchronization is applied
    public final static Random random = new Random();


    /**
     * Returns a random boolean.
     *
     * @return a random boolean.
     */
    public synchronized static boolean randomBoolean() {
        return random.nextBoolean();
    }

    /**
     * Returns a random int 0<=value<=maxvalue
     *
     * @param maxvalue
     * @return a random int
     */
    public synchronized static int randomInt(int maxvalue) {
        return random.nextInt(maxvalue + 1);
    }

    /**
     * Returns a random int that is equal or larger to zero.
     *
     * @return a random int.
     */
    public synchronized static int randomInt() {
        //Random random = new Random();
        return random.nextInt(Integer.MAX_VALUE);
    }

    /**
     * Returns a random long 0<=value<maxvalue
     *
     * @param maxvalue
     * @return
     */
    public synchronized static long randomLong(long maxvalue) {
        return Math.abs(random.nextLong() % maxvalue);
    }

    /**
     * Sleeps for a random amount of time.
     *
     * @param maxSleepMs the maximum amount of time to sleep
     */
    public static void sleepRandomMs(long maxSleepMs) {
        sleepRandom(maxSleepMs, TimeUnit.MILLISECONDS);
    }


    /**
     * Sleeps a random amount of time.
     *
     * @param maxSleep the maximum amount of time
     * @param unit
     */
    public static void sleepRandom(long maxSleep, TimeUnit unit) {
        long sleepNs = randomLong(maxSleep);

        long ms = unit.toMillis(sleepNs);
        int ns = (int) (unit.toNanos(sleepNs) % TimeUnit.MILLISECONDS.toNanos(1));
        try {
            //    System.out.println("ms "+ms+" ns "+ns);
            Thread.sleep(ms, ns);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
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
                sleepUninterruptibly(delay, TimeUnit.MILLISECONDS);

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

    //never returns a value smaller than 0
    public static double PI(long i) {
        double total = 0.0;
        for (long j = 1; j <= i; j += 4)
            total += 1.0 / j - 1.0 / (j + 2);
        return 4 * total;
    }

    public static void giveOthersAChance() {
        giveOthersAChance(3 * Delays.TINY_MS);
    }

    public static void giveOthersAChance(long ms) {
        Thread.yield();//operation increases the chance of context switches, but it is allowed to be seen as a no-op
        sleepMs(ms);
    }

    /**
     * Sleeps a certain number of milliseconds. If the calling thread is interrupted,
     * the test fails.
     * <p/>
     * Only the thread that runs the testcase should call this method.
     *
     * @param ms the number of milliseconds to giveOthersAChance.
     */
    public static void sleepMs(long ms) {
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
    public static void sleep(long period, TimeUnit unit) {
        if (unit == null) throw new NullPointerException();
        if (period <= 0)
            return;

        long periodNs = unit.toNanos(period);
        long ms = TimeUnit.NANOSECONDS.toMillis(periodNs);
        int ns = (int) (periodNs % TimeUnit.MILLISECONDS.toNanos(1));

        try {
            Thread.sleep(ms, ns);
        } catch (InterruptedException e) {
            Assert.fail("giveOthersAChance was interrupted");
        }
    }

    /**
     * Asserts that the given thread is not alive.
     *
     * @param thread the Thread to check
     */
    public static void assertNotAlive(Thread thread) {
        if (thread.isAlive()) {
            String stracktrace = stackTraceAsString(thread);
            Assert.fail(format("Thread '%s' is still alive, stacktrace: \n%s",thread,stracktrace));
        }
    }

    public static String stackTraceAsString(Thread t) {
        StackTraceElement[] trace = getStackTraceElements(t);
        if (trace == null)
            return "No stacktrace info";

        StringBuffer sb = new StringBuffer();
        for (StackTraceElement element : trace) 
            sb.append("\tat " + element+"\n");
        return sb.toString();
    }

    public static StackTraceElement[] getStackTraceElements(Thread t) {
        return Thread.getAllStackTraces().get(t);
    }

    /**
     * Asserts that the given thread is alive.
     *
     * @param thread the Thread to check.
     */
    public static void assertAlive(Thread thread) {
        assertTrue(format("Thread '%s' is not alive", thread), thread.isAlive());
    }

    public static void someCalculation(long iterations) {
        double pi = PI(iterations);
        if (pi <= -1)
            System.out.println("Benchmark exited with unrealistic value " + pi);
    }

    //todo: moven naar test support
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
    public static void joinAll(Thread... threads) {
        joinAll(2 * Delays.LONG_MS, threads);
    }

    public static void joinAll(long delayMs, Thread... threads) {
        for (int k = 0; k < threads.length; k++) {
            Thread t = threads[k];
            try {
                long startNs = System.nanoTime();
                t.join(delayMs);
                long endNs = System.nanoTime();
                delayMs -= TimeUnit.NANOSECONDS.toMillis(endNs - startNs);

                assertNotAlive(t);
            } catch (InterruptedException e) {
                Assert.fail(format("join on thread #%s was interrupted", k));
            }
        }
    }
}
