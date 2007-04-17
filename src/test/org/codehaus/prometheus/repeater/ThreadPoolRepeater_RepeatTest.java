/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.testsupport.CountingRunnable;
import org.codehaus.prometheus.testsupport.DummyRunnable;
import org.codehaus.prometheus.testsupport.SleepingRunnable;

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
        CountingRunnable task = new CountingRunnable();
        Repeatable repeatable = new RepeatableRunnable(task);

        repeater.repeat(repeatable);
        giveWorkersTimeToRun();

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
        CountingRunnable task = new CountingRunnable();
        Repeatable repeatable = new RepeatableRunnable(task);

        repeater.repeat(repeatable);
        giveWorkersTimeToRun();

        assertIsRunning();
        assertHasRepeatable(repeatable);
        task.assertExecutedOnceOrMore();
    }

    //==================== shutting down repeater ===============

    public void testShuttingDownRepeater() throws InterruptedException {
        newShuttingdownRepeater(2* DELAY_SMALL_MS);
        assertRepeatCauseRejectedExecutionException(new DummyRunnable());
        assertRepeatCauseRejectedExecutionException(null);
    }

    //================== interrupted while waiting to take place ===========

    public void testInterruptedWhileWaitingToPlaceTask() {
        Runnable originalTask = new SleepingRunnable(DELAY_MEDIUM_MS);
        Repeatable originalRepeatable = new RepeatableRunnable(originalTask);

        newRunningStrictRepeater(originalRepeatable);
        RepeatThread put = scheduleRepeat(new DummyRepeatable());

        sleepMs(DELAY_TINY_MS);
        put.assertIsStarted();
        assertIsRunning();
        assertHasRepeatable(originalRepeatable);

        put.interrupt();
        joinAll(put);
        put.assertIsInterruptedByException();
        assertIsRunning();
        assertHasRepeatable(originalRepeatable);
    }

    //============== testShutdown =========

    public void testShutdownRepeater_strict() throws InterruptedException {
        testShutdownRepeater(true);
    }

    public void testShutdownRepeater_relaxed() throws InterruptedException {
        testShutdownRepeater(false);
    }

    public void testShutdownRepeater(boolean strict) throws InterruptedException {
        newShutdownRepeater(strict);
        assertRepeatCauseRejectedExecutionException(new DummyRunnable());
        assertRepeatCauseRejectedExecutionException(null);
    }

    public void assertRepeatCauseRejectedExecutionException(Runnable task) throws InterruptedException {
        try {
            repeater.repeat(new DummyRepeatable());
            fail("RejectedExecutionException expected");
        } catch (RejectedExecutionException ex) {
            assertTrue(true);
        }
    }

    //========================================
    // test that the running task is not interrupted by repeating a task
}
