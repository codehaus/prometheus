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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unittests the {@link ThreadPoolRepeater#tryRepeat(Repeatable,long,TimeUnit)} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_TimedTryRepeatTest extends ThreadPoolRepeater_AbstractTest {

    public void testArguments() throws TimeoutException, InterruptedException {
        newRunningStrictRepeater();
        try {
            repeater.tryRepeat(new DummyRepeatable(), 1, null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
        assertIsRunning();
    }

    public void testWhileUnstarted() {
        newUnstartedStrictRepeater();

        TestRunnable task = new TestRunnable();
        Repeatable repeatable = new RepeatableRunnable(task);
        TimedTryRepeatThread tryRepeatThread = scheduleTimedTryRepeat(repeatable, 1000);
        joinAll(tryRepeatThread);

        tryRepeatThread.assertSuccess();
        assertIsRunning();
        assertHasRepeatable(repeatable);

        giveOthersAChance();

        task.assertExecutedOnceOrMore();
    }

    public void testRepeatNull() throws InterruptedException, TimeoutException {
        TestRunnable task = new TestRunnable();
        RepeatableRunnable repeatable = new RepeatableRunnable(task);
        newRunningStrictRepeater(repeatable);

        TimedTryRepeatThread tryRepeatThread = scheduleTimedTryRepeat(null, Delays.SMALL_MS);
        joinAll(tryRepeatThread);
        assertIsRunning();
        assertHasRepeatable(null);
        task.assertNotRunningAnymore();
    }

    public void testWhileRunning_someWaitingNeeded() {
        newRunningStrictRepeater(new RepeatableRunnable(newSleepingRunnable(Delays.MEDIUM_MS)));
        giveOthersAChance();

        TestRunnable task = new TestRunnable();
        Repeatable repeatable = new RepeatableRunnable(task);
        TimedTryRepeatThread tryRepeatThread = scheduleTimedTryRepeat(repeatable, Delays.LONG_MS);

        giveOthersAChance();
        tryRepeatThread.assertIsStarted();

        joinAll(tryRepeatThread);
        tryRepeatThread.assertSuccess();
        assertIsRunning();
        assertHasRepeatable(repeatable);

        task.assertExecutedOnceOrMore();
    }


    public void testNegativeTimeout() throws InterruptedException {
        TestRunnable task = new TestRunnable();
        Repeatable repeatable = new RepeatableRunnable(task);
        newRunningStrictRepeater();

        try {
            repeater.tryRepeat(repeatable, -1, TimeUnit.MILLISECONDS);
            fail("TimeoutException expected");
        } catch (TimeoutException e) {
            assertTrue(true);
        }

        task.assertNotRunningAnymore();
        assertIsRunning();
        assertHasRepeatable(null);
    }


    public void testTooMuchWaiting() {
        Repeatable originalRepeatable = new RepeatableRunnable(newSleepingRunnable(Delays.MEDIUM_MS));
        newRunningStrictRepeater(originalRepeatable);

        TestRunnable newTask = new TestRunnable();
        TimedTryRepeatThread tryRepeatThread = scheduleTimedTryRepeat(new RepeatableRunnable(newTask), Delays.TINY_MS);
        joinAll(tryRepeatThread);

        tryRepeatThread.assertIsTimedOut();
        newTask.assertNotRunningAnymore();
        assertIsRunning();
        assertHasRepeatable(originalRepeatable);
    }

    public void testWhileShuttingDown() throws TimeoutException, InterruptedException {
        newShuttingdownRepeater(Delays.MEDIUM_MS);
        giveOthersAChance();

        Repeatable originalTask = repeater.getLendableRef().peek();
        TestRunnable task = new TestRunnable();
        TimedTryRepeatThread tryRepeatThread = scheduleTimedTryRepeat(new RepeatableRunnable(task), 1);
        joinAll(tryRepeatThread);
        tryRepeatThread.assertIsTerminatedWithThrowing(RejectedExecutionException.class);
        assertIsShuttingdown();
        assertHasRepeatable(originalTask);
        task.assertNotExecuted();
    }

    public void testWhileForcedShuttingdown() {
        newForcedShuttingdownRepeater(Delays.MEDIUM_MS, 1);
        giveOthersAChance();

        Repeatable originalTask = repeater.getLendableRef().peek();
        TestRunnable task = new TestRunnable();
        TimedTryRepeatThread tryRepeatThread = scheduleTimedTryRepeat(new RepeatableRunnable(task), 1);
        joinAll(tryRepeatThread);
        tryRepeatThread.assertIsTerminatedWithThrowing(RejectedExecutionException.class);
        assertIsShuttingdown();
        assertHasRepeatable(originalTask);
        task.assertNotExecuted();
    }

    public void testInterruptedWhileWaiting() {
        Repeatable originalRepeatable = new RepeatableRunnable(newSleepingRunnable(Delays.LONG_MS));
        newRunningStrictRepeater(originalRepeatable);

        TimedTryRepeatThread tryRepeatThread = scheduleTimedTryRepeat(new DummyRepeatable(), Delays.MEDIUM_MS);
        giveOthersAChance();
        tryRepeatThread.assertIsStarted();

        //interrupt the thread 
        tryRepeatThread.interrupt();
        joinAll(tryRepeatThread);
        tryRepeatThread.assertIsTerminatedByInterruptedException();
        assertIsRunning();
        assertHasRepeatable(originalRepeatable);
    }

    public void testWhileShutdown() throws InterruptedException {
        newShutdownRepeater();
        assertRepeatIsRejected();
        assertIsShutdown();
        assertHasRepeatable(null);
    }

    private void assertRepeatIsRejected() throws InterruptedException {
        try {
            repeater.repeat(new DummyRepeatable());
            fail("RejectedExecutionException expected");
        } catch (RejectedExecutionException ex) {
            assertTrue(true);
        }
    }

    //todo test that the running task is not interrupted by repeating a task
}

