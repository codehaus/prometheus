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

    public void testNotStarted() {
        newUnstartedStrictRepeater();

        shutdownNow();

        assertIsShutdown();
    }

    public void testRunningTask() throws InterruptedException {
        CountingRunnable task = new CountingRunnable();
        newRunningStrictRepeater(new RepeatableRunnable(task));
        assertShutdownHappens();
        task.assertNotRunningAnymore();
        assertActualPoolSize(0);
    }

    public void testShutdownNowCantForceNonInterruptibleTaskToEnd() {
        NonInterruptableSleepingRunnable task = new NonInterruptableSleepingRunnable(DELAY_SMALL_MS);
        newRunningStrictRepeater(new RepeatableRunnable(task));

        shutdownNow();
        assertIsShuttingdown();

        assertIsShuttingdown();
        awaitShutdown();
        assertIsShutdown();
    }

    public void testRunningNullTask() {
        newRunningStrictRepeater();
        assertShutdownHappens();
        assertActualPoolSize(0);
    }

    public void testStartedEmptyPool() {
        newRunningRepeater(true, 0);
        shutdownNow();
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
        giveOthersAChance();
        task.assertIsStarted();

        //now shutdown the repeater, this should interrupt the placement of the task
        shutdownNow();

        giveOthersAChance();
        task.assertIsInterrupted();

        awaitShutdown();
        stopwatch.stop();

        Thread.yield();
        task.assertIsInterrupted();
        //if the threads were not interrupted, the elapsed time should be around 10 seconds and not 1.
        stopwatch.assertElapsedSmallerThanMs(1000);
    }

    private void shutdownNow() {
        ShutdownNowThread shutdownNowThread = scheduleShutdownNow();
        joinAll(shutdownNowThread);
        shutdownNowThread.assertIsTerminatedNormally();
    }

    private void awaitShutdown() {
        AwaitShutdownThread awaitShutdownThread = scheduleAwaitShutdown();
        joinAll(awaitShutdownThread);
        awaitShutdownThread.assertIsTerminatedNormally();
    }

    public void assertShutdownHappens() {
        shutdownNow();

        awaitShutdown();

        assertIsShutdown();
    }
}
