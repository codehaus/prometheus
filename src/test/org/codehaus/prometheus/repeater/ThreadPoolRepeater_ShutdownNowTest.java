/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.testsupport.CountingRunnable;
import org.codehaus.prometheus.testsupport.SleepingRunnable;
import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;
import static org.codehaus.prometheus.testsupport.TestUtil.sleepMs;
import org.codehaus.prometheus.testsupport.UninterruptableSleepingRunnable;

/**
 * Unittests the {@link ThreadPoolRepeater#shutdownNow()} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_ShutdownNowTest extends ThreadPoolRepeater_AbstractTest {

    public void testWhileUnstarted() {
        newUnstartedStrictRepeater();

        spawned_shutdownNow();

        assertIsShutdown();
    }

    public void testWhileRunning_repeatableIsRunning() throws InterruptedException {
        CountingRunnable task = new CountingRunnable();
        newRunningStrictRepeater(new RepeatableRunnable(task));
        assertShutdownHappens();
        task.assertNotRunningAnymore();
        assertActualPoolSize(0);
    }

    public void testWhileRunning_noRepeatableIsSet() {
        newRunningStrictRepeater();
        assertShutdownHappens();
        assertActualPoolSize(0);
    }

    public void testShutdownNowCantForceNonInterruptibleTaskToEnd() {
        UninterruptableSleepingRunnable task = new UninterruptableSleepingRunnable(DELAY_SMALL_MS);
        newRunningStrictRepeater(new RepeatableRunnable(task));

        spawned_shutdownNow();
        assertIsShuttingdown();

        assertIsShuttingdown();
        spawned_awaitShutdown();
        assertIsShutdown();
    }

    public void testStarted_poolIsEmpty() {
        newRunningRepeater(true, 0);
        spawned_shutdownNow();
        assertIsShutdown();
        assertActualPoolSize(0);
    }

    public void testWhileShuttingdown() {
        newShuttingdownRepeater(DELAY_SMALL_MS);
        repeater.shutdownNow();
        assertIsShuttingdown();
        assertActualPoolSize(1);

        sleepMs(2 * DELAY_SMALL_MS);
        assertIsShutdown();
        assertActualPoolSize(0);
    }

    public void testWhileForcedShuttingdown(){
        //todo
    }

    public void testWhileShutdown() {
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
        spawned_shutdownNow();

        giveOthersAChance();
        task.assertIsInterrupted();

        spawned_awaitShutdown();
        stopwatch.stop();

        Thread.yield();
        task.assertIsInterrupted();
        //if the threads were not interrupted, the elapsed time should be around 10 seconds and not 1.
        stopwatch.assertElapsedSmallerThanMs(1000);
    }

    public void assertShutdownHappens() {
        spawned_shutdownNow();

        spawned_awaitShutdown();

        assertIsShutdown();
    }
}
