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
        testWhileUnstarted(true);
    }

    public void testWhileUnstarted_startUninterrupted() {
        testWhileUnstarted(false);
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
        testWhileRunning_SomeWaitingNeeded(true);
    }

    public void testWhileRunning_SomeWaitingNeeded_startUninterrupted() {
        testWhileRunning_SomeWaitingNeeded(false);
    }

    public void testWhileRunning_SomeWaitingNeeded(boolean startInterrupted) {
        Runnable originalTask = new UninterruptableSleepingRunnable(5000);
        RepeatableRunnable originalRepeatable = new RepeatableRunnable(originalTask);
        newRunningStrictRepeater(originalRepeatable);

        CountingRunnable task = new CountingRunnable();
        TryRepeatThread t = scheduleTryRepeat(new RepeatableRunnable(task), startInterrupted);

        joinAll(t);
        t.assertFailure();
        t.assertIsTerminatedWithInterruptStatus(startInterrupted);
        assertIsRunning();
        assertHasRepeatable(originalRepeatable);
        assertActualPoolSize(1);

        giveOthersAChance();
        task.assertNotExecuted();
    }

    //=====================================================

    public void testNoWaitingNeeded_startInterrupted() {
        testNoWaitingNeeded(true);
    }

    public void testNoWaitingNeeded_startUninterrupted() {
        testNoWaitingNeeded(false);
    }

    public void testNoWaitingNeeded(boolean startInterrupted) {
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
        testWhileShuttingdown(true);
    }

    public void testWhileShuttingdown_startUninterrupted() {
        testWhileShuttingdown(false);
    }

    public void testWhileShuttingdown(boolean startInterrupted) {
        newShuttingdownRepeater(1000);
        Repeatable originalTask = repeater.getLendableRef().peek();

        CountingRunnable task = new CountingRunnable();
        TryRepeatThread t = scheduleTryRepeat(new RepeatableRunnable(task), startInterrupted);

        joinAll(t);
        t.assertIsTerminatedWithThrowing(RejectedExecutionException.class);
        t.assertIsTerminatedWithInterruptStatus(startInterrupted);
        task.assertNotExecuted();
        assertHasRepeatable(originalTask);
        assertActualPoolSize(1);
        assertIsShuttingdown();
    }

    public void testWhileForcedShuttingdown(){
        //todo
    }

    //=====================================================

    public void testShutdown_startInterrupted() {
        testShutdown(true);
    }

    public void testShutdown_startUninterrupted() {
        testShutdown(false);
    }

    public void testShutdown(boolean startInterrupted) {
        newShutdownRepeater();
        CountingRunnable task = new CountingRunnable();
        TryRepeatThread t = scheduleTryRepeat(new RepeatableRunnable(task), startInterrupted);

        joinAll(t);
        t.assertIsTerminatedWithThrowing(RejectedExecutionException.class);
        t.assertIsTerminatedWithInterruptStatus(startInterrupted);
        task.assertNotExecuted();
        assertIsShutdown();
        assertActualPoolSize(0);
    }

    //==========================================================
    // test that the running task is not interrupted by repeating a task
}
