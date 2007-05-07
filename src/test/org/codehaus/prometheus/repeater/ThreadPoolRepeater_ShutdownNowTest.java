/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.testsupport.CountingRunnable;
import org.codehaus.prometheus.testsupport.NonInterruptableSleepingRunnable;
import org.codehaus.prometheus.testsupport.SleepingRunnable;

/**
 * Unittests the {@link ThreadPoolRepeater#shutdownNow()} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_ShutdownNowTest extends ThreadPoolRepeater_AbstractTest {

    public void testShutdownIsBlocked() {
        fail();
    }

    public void testJobExecutingRepeater() {
        newRunningStrictRepeater();
    }

    public void testNotStarted() {
        newUnstartedStrictRepeater();
        repeater.shutdownNow();
        assertIsShutdown();
    }

    public void testRunningTask() throws InterruptedException {
        CountingRunnable task = new CountingRunnable();
        newRunningStrictRepeater(new RepeatableRunnable(task));
        assertShutdownHappens();
        task.assertNotRunningAnymore();
        assertActualPoolSize(0);
    }

    public void testDelayedShutdown() {
        NonInterruptableSleepingRunnable task = new NonInterruptableSleepingRunnable(DELAY_SMALL_MS);
        newRunningStrictRepeater(new RepeatableRunnable(task));

        repeater.shutdownNow();
        assertIsShuttingdown();

        sleepMs(2 * DELAY_SMALL_MS);
        assertIsShutdown();
    }

    public void testRunningNullTask() {
        newRunningStrictRepeater();
        assertShutdownHappens();
        assertActualPoolSize(0);
    }

    public void testStartedEmptyPool() {
        repeater = new ThreadPoolRepeater(0);
        repeater.start();
        repeater.shutdownNow();
        assertIsShutdown();
        assertActualPoolSize(0);
    }

    public void testShutdownWhileShuttingDown() {
        newShuttingdownRepeater(DELAY_SMALL_MS);
        repeater.shutdownNow();
        assertIsShuttingdown();
        assertActualPoolSize(1);

        sleepMs(2 * DELAY_SMALL_MS);
        assertIsShutdown();
        assertActualPoolSize(0);
    }

    public void testShutdownWhileShutdown() {
        newShutdownRepeater();
        repeater.shutdownNow();
        assertIsShutdown();
        assertActualPoolSize(0);
    }


    //make sure that the workers are interrupted, when the threadpoolrepeater shuts down
    public void testShutdownInterruptsWorkers() throws InterruptedException {
        stopwatch.start();
        SleepingRunnable task = new SleepingRunnable(DELAY_LONG_MS);
        newRunningStrictRepeater(new RepeatableRunnable(task));

        //make sure the task is waiting.
        sleepMs(DELAY_SMALL_MS);
        task.assertIsStarted();

        //now shutdown the repeater, this should interrupt the placement of the task
        repeater.shutdownNow();
        sleepMs(DELAY_SMALL_MS);
        task.assertIsInterrupted();

        //wait till the repeater has completely shut down.
        repeater.awaitShutdown();
        stopwatch.stop();

        Thread.yield();
        task.assertIsInterrupted();
        //if the threads were not interrupted, the elapsed time should be around 10 seconds and not 1.
        stopwatch.assertElapsedSmallerThanMs(1000);
    }

    public void assertShutdownHappens() {
        repeater.shutdownNow();
        //this should give the repeater enough time to
        //let the worker thread shut down.
        sleepMs(DELAY_SMALL_MS);

        assertIsShutdown();
    }
}
