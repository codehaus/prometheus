package org.codehaus.prometheus.concurrenttesting;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A Facade for creating TestSupport, TestCallable's etc. Objects can always be created
 * by using their constructor, but using a factory with self explaining factory methods
 * can be easier to use.
 *
 * @author Peter Veentjer.
 */
public class TestSupport {

    public static TestRunnable newDummyRunnable() {
        return new TestRunnable();
    }

    public static TestRunnable newTestedRunnable(Runnable task) {
        return new TestRunnable(task);
    }

    public static TestRunnable newThrowingTestRunnable() {
        return newThrowingTestRunnable(new RuntimeException());
    }

    public static TestRunnable newThrowingTestRunnable(RuntimeException ex) {
        return new TestRunnable(ex);
    }

    public static SleepingRunnable newUninterruptableSleepingRunnable(long periodMs) {
        return new SleepingRunnable(periodMs, TimeUnit.MILLISECONDS, false);
    }

    public static SleepingRunnable newEonSleepingRunnable() {
        return new SleepingRunnable(Delays.EON_MS, TimeUnit.MILLISECONDS, true);
    }

    public static SleepingRunnable newSleepingRunnable(long periodMs) {
        return new SleepingRunnable(periodMs, TimeUnit.MILLISECONDS, true);
    }

    public static <E> TestCallable newDummyCallable(E result) {
        return new TestCallable<E>(result);
    }

    public static TestCallable newThrowingCallable(Exception ex) {
        return new TestCallable(ex);
    }

    public static StressRunnable newStressRunnable() {
        return new StressRunnable();
    }

    public static StressRunnable newStressRunnable(int iterations) {
        return new StressRunnable(iterations);
    }

    /**
     * A Factory method for creating TestSupport that sleeps.
     *
     * @param sleepMs       the amount of milliseconds to sleep.
     * @param interruptible if the sleeping should be interruptible or not
     * @return the created TestRunnable
     */
    public static TestRunnable newSleepingRunnable(long sleepMs, boolean interruptible) {
        return new SleepingRunnable(sleepMs, TimeUnit.MILLISECONDS, interruptible);
    }

    public static List<TestRunnable> newUninterruptibleSleepingRunnables(long sleepMs, int count) {
        List<TestRunnable> list = new LinkedList<TestRunnable>();
        for (int k = 0; k < count; k++) {
            TestRunnable runnable = newSleepingRunnable(sleepMs, false);
            list.add(runnable);
        }
        return list;
    }

    //we don't want any instances
    private TestSupport() {
    }
}
