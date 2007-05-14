/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.testsupport.CountingRunnable;
import org.codehaus.prometheus.testsupport.NonInterruptableSleepingRunnable;

import java.util.concurrent.RejectedExecutionException;

/**
 * Unittests the {@link ThreadPoolRepeater#tryRepeat(Repeatable)} method. 
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_TryRepeatTest extends ThreadPoolRepeater_AbstractTest {

    public void testUnstarted_startInterrupted() {
        testUnstarted(true);
    }

    public void testUnstarted_startUninterrupted() {
        testUnstarted(false);
    }

    public void testUnstarted(boolean startInterrupted) {
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

    public void testRunningSomeWaitingNeeded_startInterrupted() {
        testRunningSomeWaitingNeeded(true);
    }

    public void testRunningSomeWaitingNeeded_startUninterrupted() {
        testRunningSomeWaitingNeeded(false);
    }

    public void testRunningSomeWaitingNeeded(boolean startInterrupted) {
        Runnable originalTask = new NonInterruptableSleepingRunnable(5000);
        RepeatableRunnable originalRepeatable = new RepeatableRunnable(originalTask);
        newRunningStrictRepeater(originalRepeatable);

        CountingRunnable task = new CountingRunnable();
        TryRepeatThread t = scheduleTryRepeat( new RepeatableRunnable(task),startInterrupted);

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

    public void testShuttingdown_startInterrupted() {
        testShuttingdown(true);
    }

    public void testShuttingdown_startUninterrupted() {
        testShuttingdown(false);
    }

    public void testShuttingdown(boolean startInterrupted) {
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
