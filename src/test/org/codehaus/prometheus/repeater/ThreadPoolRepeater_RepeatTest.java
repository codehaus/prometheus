/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.testsupport.TestRunnable;
import org.codehaus.prometheus.testsupport.Delays;
import static org.codehaus.prometheus.testsupport.TestSupport.newSleepingRunnable;
import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.joinAll;

import java.util.concurrent.RejectedExecutionException;

/**
 * Unittests the {@link ThreadPoolRepeater#repeat(Repeatable)} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_RepeatTest extends ThreadPoolRepeater_AbstractTest {


    //==================================================
    public void testUnstartedEmptyRepeater_repeatNullTask() throws InterruptedException {
        newUnstartedStrictRepeater();
        repeater.repeat(null);

        assertIsRunning();
        assertHasRepeatable(null);
    }

    public void testUnstartedEmptyRepeater_repeatSomeTask() throws InterruptedException {
        newUnstartedStrictRepeater();
        TestRunnable task = new TestRunnable();
        Repeatable repeatable = new RepeatableRunnable(task);

        repeater.repeat(repeatable);
        giveOthersAChance();

        assertIsRunning();
        assertHasRepeatable(repeatable);
        task.assertExecutedOnceOrMore();
    }

    public void testStartedEmptyRepeater_repeatNullTask() throws InterruptedException {
        newRunningStrictRepeater();

        repeater.repeat(null);

        assertIsRunning();
        assertHasRepeatable(null);
    }

    public void testStartedEmptyRepeater_repeatSomeTask() throws InterruptedException {
        newRunningStrictRepeater();
        TestRunnable task = new TestRunnable();
        Repeatable repeatable = new RepeatableRunnable(task);

        repeater.repeat(repeatable);
        giveOthersAChance();

        assertIsRunning();
        assertHasRepeatable(repeatable);
        task.assertExecutedOnceOrMore();
    }

    //==================== shutting down repeater ===============

    public void testWhileShuttingdown() throws InterruptedException {
        newShuttingdownRepeater(2 * Delays.MEDIUM_MS);
        assertRepeatIsRejected(new DummyRepeatable());
        assertRepeatIsRejected(null);
    }

    public void testWhileForcedShuttingdown() throws InterruptedException {
        newForcedShuttingdownRepeater(Delays.LONG_MS,10);

        assertRepeatIsRejected(new DummyRepeatable());
    }

    //================== interrupted while waiting to take place ===========

    public void testInterruptedWhileWaitingToPlaceTask() {
        Runnable originalTask = newSleepingRunnable(Delays.MEDIUM_MS);
        Repeatable originalRepeatable = new RepeatableRunnable(originalTask);

        newRunningStrictRepeater(originalRepeatable);
        RepeatThread put = scheduleRepeat(new DummyRepeatable());
        giveOthersAChance();
        put.assertIsStarted();
        assertIsRunning();
        assertHasRepeatable(originalRepeatable);

        put.interrupt();
        joinAll(put);
        put.assertIsTerminatedByInterruptedException();
        assertIsRunning();
        assertHasRepeatable(originalRepeatable);
    }

    //============== testWhileShutdown =========

    public void testShutdownRepeater_strict() throws InterruptedException {
        testShutdownRepeater(true);
    }

    public void testShutdownRepeater_relaxed() throws InterruptedException {
        testShutdownRepeater(false);
    }

    public void testShutdownRepeater(boolean strict) throws InterruptedException {
        newShutdownRepeater(strict);
        assertRepeatIsRejected(new DummyRepeatable());
        assertRepeatIsRejected(null);
    }

    public void assertRepeatIsRejected(Repeatable task) throws InterruptedException {
        try {
            repeater.repeat(task);
            fail("RejectedExecutionException expected");
        } catch (RejectedExecutionException ex) {
            assertTrue(true);
        }
    }
}
