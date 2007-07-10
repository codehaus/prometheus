/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.testsupport.CountingRunnable;
import org.codehaus.prometheus.testsupport.UninterruptableSleepingRunnable;
import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;

import java.util.concurrent.RejectedExecutionException;

/**
 * Unittests the {@link ThreadPoolRepeater#tryRepeat(Repeatable)} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_TryRepeatTest extends ThreadPoolRepeater_AbstractTest {

    public void testWhileUnstarted_startInterrupted() {
        testWhileUnstarted(START_INTERRUPTED);
    }

    public void testWhileUnstarted_startUninterrupted() {
        testWhileUnstarted(START_UNINTERRUPTED);
    }

    public void testWhileUnstarted(boolean startInterrupted) {
        newUnstartedStrictRepeater();
        CountingRunnable task = new CountingRunnable();
        Repeatable repeatable = new RepeatableRunnable(task);

        TryRepeatThread t = scheduleTryRepeat(repeatable, startInterrupted);
        joinAll(t);
        t.assertSuccess();
        t.assertIsTerminatedWithInterruptStatus(startInterrupted);

        assertHasRepeatable(repeatable);
        assertActualPoolSize(1);
        assertIsRunning();

        giveOthersAChance();
        task.assertExecutedOnceOrMore();
    }

    //=====================================================

    public void testWhileRunning_someWaitingNeeded_startInterrupted() {
        testWhileRunning_someWaitingNeeded(START_INTERRUPTED);
    }

    public void testWhileRunning_SomeWaitingNeeded_startUninterrupted() {
        testWhileRunning_someWaitingNeeded(START_UNINTERRUPTED);
    }

    public void testWhileRunning_someWaitingNeeded(boolean startInterrupted) {
        Runnable originalTask = new UninterruptableSleepingRunnable(DELAY_LONG_MS);
        RepeatableRunnable originalRepeatable = new RepeatableRunnable(originalTask);
        newRunningStrictRepeater(originalRepeatable);

        CountingRunnable task = new CountingRunnable();
        TryRepeatThread t = scheduleTryRepeat(new RepeatableRunnable(task), startInterrupted);

        joinAll(t);
        t.assertFailed();
        t.assertIsTerminatedWithInterruptStatus(startInterrupted);
        assertIsRunning();
        assertHasRepeatable(originalRepeatable);
        assertActualPoolSize(1);

        giveOthersAChance();
        task.assertNotExecuted();
    }

    //=====================================================

    public void testWhileRunning_noWaitingNeeded_startInterrupted() {
        testWhileRunning_NoWaitingNeeded(START_INTERRUPTED);
    }

    public void testWhileRunning_noWaitingNeeded_startUninterrupted() {
        testWhileRunning_NoWaitingNeeded(START_UNINTERRUPTED);
    }

    public void testWhileRunning_NoWaitingNeeded(boolean startInterrupted) {
        newRunningStrictRepeater();
        CountingRunnable task = new CountingRunnable();
        Repeatable repeatable = new RepeatableRunnable(task);
        TryRepeatThread t = scheduleTryRepeat(repeatable, startInterrupted);

        joinAll(t);
        t.assertSuccess();
        t.assertIsTerminatedWithInterruptStatus(startInterrupted);
        assertIsRunning();
        assertHasRepeatable(repeatable);
        assertActualPoolSize(1);

        giveOthersAChance();
        task.assertExecutedOnceOrMore();
    }

    //=====================================================

    public void testWhileShuttingdown_startInterrupted() {
        testWhileShuttingdown(START_INTERRUPTED);
    }

    public void testWhileShuttingdown_startUninterrupted() {
        testWhileShuttingdown(START_UNINTERRUPTED);
    }

    public void testWhileShuttingdown(boolean startInterrupted) {
        newShuttingdownRepeater(DELAY_LONG_MS);
        Repeatable originalTask = repeater.getLendableRef().peek();

        CountingRunnable task = new CountingRunnable();
        TryRepeatThread t = scheduleTryRepeat(new RepeatableRunnable(task), startInterrupted);

        joinAll(t);
        t.assertIsTerminatedWithThrowing(RejectedExecutionException.class);
        t.assertIsTerminatedWithInterruptStatus(startInterrupted);
        task.assertNotExecuted();
        assertHasRepeatable(originalTask);
        assertActualPoolSize(1);
        repeaterThreadFactory.assertCreatedAndAliveCount(1);
        assertIsShuttingdown();
    }

    //=====================================================

    public void testWhileShutdown_startInterrupted() {
        testWhileShutdown(START_INTERRUPTED);
    }

    public void testWhileShutdown_startUninterrupted() {
        testWhileShutdown(START_UNINTERRUPTED);
    }

    public void testWhileShutdown(boolean startInterrupted) {
        newShutdownRepeater();
        CountingRunnable task = new CountingRunnable();
        TryRepeatThread t = scheduleTryRepeat(new RepeatableRunnable(task), startInterrupted);

        joinAll(t);
        t.assertIsTerminatedWithThrowing(RejectedExecutionException.class);
        t.assertIsTerminatedWithInterruptStatus(startInterrupted);
        task.assertNotExecuted();
        assertIsShutdown();
        assertActualPoolSize(0);
        repeaterThreadFactory.assertCreatedAndTerminatedCount(1);
    }

    //==========================================================
    // test that the running task is not interrupted by repeating a task
}
