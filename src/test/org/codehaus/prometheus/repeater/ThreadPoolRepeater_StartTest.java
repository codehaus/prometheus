/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.concurrenttesting.TestRunnable;
import org.codehaus.prometheus.concurrenttesting.Delays;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;

/**
 * Unittests the {@link ThreadPoolRepeater#start()} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_StartTest extends ThreadPoolRepeater_AbstractTest {

    public void testWhileUnstarted_noTask() {
        newUnstartedStrictRepeater();

        repeater.start();

        assertIsRunning();
        assertActualPoolSize(1);
    }

    public void testWhileUnstarted_hasTask() {
        TestRunnable task = new TestRunnable();
        newUnstartedRepeater(task);

        repeater.start();
        giveOthersAChance();

        assertIsRunning();
        assertActualPoolSize(1);
        task.assertExecutedOnceOrMore();
    }

    public void testWhileRunning_noTask() {
        newRunningStrictRepeater();

        assertStartIsIgnored();
        assertActualPoolSize(1);
    }

    public void testWhileRunning_hasTask() {
        newRunningStrictRepeater(new DummyRepeatable());

        assertStartIsIgnored();
        assertActualPoolSize(1);
    }

    public void testWhileUnstarted_noThreads() {
        TestRunnable task = new TestRunnable();
        Repeatable repeatable = new RepeatableRunnable(task);

        repeater = new ThreadPoolRepeater(repeatable, 0);
        repeater.start();
        assertIsRunning();

        giveOthersAChance();
        task.assertNotExecuted();
        assertActualPoolSize(0);
    }

    public void testWhileShuttingdown() throws InterruptedException {
        newShuttingdownRepeater(2 * Delays.SMALL_MS);

        assertStartCausesIllegalStateException();
        assertActualPoolSize(1);
    }

    public void testWhileForcedShuttingdown()throws InterruptedException{
        int poolsize = 10;
        newForcedShuttingdownRepeater(Delays.LONG_MS,poolsize);

        assertStartCausesIllegalStateException();
        assertActualPoolSize(poolsize);
        repeaterThreadFactory.assertAliveCount(poolsize);
    }

    public void testWhileShutdown() throws InterruptedException {
        newShutdownRepeater();

        assertStartCausesIllegalStateException();
        assertActualPoolSize(0);
    }

    public void assertStartIsIgnored() {
        RepeaterServiceState oldState = repeater.getState();

        repeater.start();

        assertEquals(oldState, repeater.getState());
    }

    public void assertStartCausesIllegalStateException() {
        RepeaterServiceState oldState = repeater.getState();
        try {
            repeater.start();
            fail("IllegalStateException expected");
        } catch (IllegalStateException ex) {
            assertTrue(true);
        }
        assertEquals(oldState, repeater.getState());
    }
}
