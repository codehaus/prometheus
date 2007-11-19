/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.concurrenttesting.SleepingRunnable;
import org.codehaus.prometheus.concurrenttesting.TestRunnable;
import org.codehaus.prometheus.concurrenttesting.Delays;
import static org.codehaus.prometheus.concurrenttesting.TestSupport.newSleepingRunnable;
import static org.codehaus.prometheus.concurrenttesting.TestSupport.newUninterruptableSleepingRunnable;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.sleepMs;

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
        TestRunnable task = new TestRunnable();
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
        SleepingRunnable task = newUninterruptableSleepingRunnable(Delays.SMALL_MS);
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
        newShuttingdownRepeater(Delays.SMALL_MS);
        repeater.shutdownNow();
        assertIsShuttingdown();
        assertActualPoolSize(1);

        sleepMs(2 * Delays.SMALL_MS);
        assertIsShutdown();
        assertActualPoolSize(0);
    }

    public void testWhileForcedShuttingdown(){
        int poolsize = 10;
        newForcedShuttingdownRepeater(Delays.LONG_MS,poolsize);

        spawned_shutdownNow();

        giveOthersAChance();
        assertIsShuttingdown();
        assertActualPoolSize(poolsize);
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
        SleepingRunnable task = newSleepingRunnable(Delays.LONG_MS);
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

        giveOthersAChance();
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
