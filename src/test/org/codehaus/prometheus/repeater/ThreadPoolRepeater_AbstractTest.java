/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.joinAll;
import static org.codehaus.prometheus.testsupport.TestSupport.newUninterruptableSleepingRunnable;
import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.testsupport.TracingThreadFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class ThreadPoolRepeater_AbstractTest extends ConcurrentTestCase {

    public volatile ThreadPoolRepeater repeater;
    public volatile TracingThreadFactory repeaterThreadFactory;

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        //if a repeater is available, make sure that is can shut down
        if (repeater != null) {
            spawned_shutdownNow();

            spawned_awaitShutdown();
        }
    }

    public void spawned_repeat(Runnable task) {
        spawned_repeat((Repeatable) new RepeatableRunnable(task));
    }

    public void spawned_repeat(Repeatable repeatable) {
        RepeatThread t = scheduleRepeat(repeatable);
        joinAll(t);
        t.assertIsTerminatedNormally();
    }

    public void spawned_shutdownNow() {
        ShutdownNowThread t = scheduleShutdownNow();
        joinAll(t);
        t.assertIsTerminatedNormally();
    }

    public void spawned_awaitShutdown() {
        AwaitShutdownThread t = scheduleAwaitShutdown();
        joinAll(t);
        t.assertIsTerminatedNormally();
        assertIsShutdown();
    }

    public void spawned_shutdown() {
        ShutdownThread t = scheduleShutdown();
        joinAll(t);
        t.assertIsTerminatedNormally();
        RepeaterServiceState state = repeater.getState();
        assertTrue(state == RepeaterServiceState.shutdown || state == RepeaterServiceState.shuttingdown);
    }

    public void newShutdownRepeater() {
        newRunningStrictRepeater();
        spawned_shutdownNow();
        spawned_awaitShutdown();
        assertIsShutdown();
    }

    public void newShutdownRepeater(boolean strict) {
        newRunningRepeater(strict);
        spawned_shutdown();
        spawned_awaitShutdown();
        assertIsShutdown();
    }

    public void newRunningStrictRepeater() {
        newUnstartedStrictRepeater();
        repeater.start();
        assertIsRunning();
    }

    public void newRunningRepeater(boolean strict) {
        newUnstartedRepeater(strict);
        repeater.start();
        assertIsRunning();
    }

    public void newRunningRepeater(boolean strict, int poolsize) {
        newUnstartedRepeater(strict, poolsize);
        repeater.start();
        assertIsRunning();
    }

    public void newRunningRepeater(int poolsize) {
        newUnstartedRepeater(false, poolsize);
        repeater.start();
        assertIsRunning();
    }

    public void newRunningRepeater(boolean strict, Repeatable task, int poolsize) {
        newUnstartedRepeater(strict, poolsize);
        try {
            repeater.repeat(task);
            //give the worker in the threadpool time to spawned_start the task
            //If this yield is removed, the thread is running, but maybe has not entered his
            //runWork method. If the shutdownNow is called before the runWork method
            //has runWork, the worker thread sees that the repeater is shutting down,
            //so the task is not going to be executed, and this is not
            //what we want.
            giveOthersAChance();
        } catch (InterruptedException e) {
            fail();
        }
        assertIsRunning();
    }

    public void newRunningStrictRepeater(Repeatable task) {
        newRunningRepeater(true, task, 1);
    }

    public void newRunningStrictRepeater(Repeatable task, int poolsize) {
        newRunningRepeater(true, task, poolsize);
    }


    public void newUnstartedStrictRepeater() {
        repeaterThreadFactory = new TracingThreadFactory();
        repeater = new ThreadPoolRepeater(true, null, 1, repeaterThreadFactory);
        assertIsUnstarted();
    }

    public void newUnstartedRepeater(Runnable task) {
        repeater = new ThreadPoolRepeater(new RepeatableRunnable(task), 1);
        assertIsUnstarted();
    }

    public void newUnstartedRepeater(boolean strict) {
        newUnstartedRepeater(strict, 1);
    }

    public void newUnstartedRepeater(boolean strict, int poolsize) {
        repeaterThreadFactory = new TracingThreadFactory();
        repeater = new ThreadPoolRepeater(strict, null, poolsize, repeaterThreadFactory);
        assertIsUnstarted();
    }

    public void newForcedShuttingdownRepeater(long timeMs, int poolsize) {
        newRunningStrictRepeater(new RepeatableRunnable(newUninterruptableSleepingRunnable(timeMs)), poolsize);
        repeater.shutdownNow();
        assertIsShuttingdown();
    }


    public void newShuttingdownRepeater(long timeMs) {
        newRunningStrictRepeater(new RepeatableRunnable(newUninterruptableSleepingRunnable(timeMs)));
        repeater.shutdown();
        assertIsShuttingdown();
    }

    public void newShuttingdownRepeater(boolean strict, long timeMs) {
        newRunningRepeater(strict, new RepeatableRunnable(newUninterruptableSleepingRunnable(timeMs)),1);
        repeater.shutdown();
        assertIsShuttingdown();
    }

    public void assertHasEndTaskStrategy() {
        assertTrue(repeater.getExecutionPolicy() instanceof EndTaskPolicy);
    }

    public void assertHasExecutionPolicy(ExecutionPolicy expected) {
        assertSame(expected, repeater.getExecutionPolicy());
    }

    public void assertIsUnstarted() {
        assertEquals(RepeaterServiceState.unstarted, repeater.getState());
    }

    public void assertIsShutdown() {
        assertEquals(RepeaterServiceState.shutdown, repeater.getState());
        assertActualPoolSize(0);
        //todo: desired poolsize
        if (repeaterThreadFactory != null)
            repeaterThreadFactory.assertAllAreNotAlive();
    }

    public void assertIsRunning() {
        assertEquals(RepeaterServiceState.running, repeater.getState());
    }

    public void assertIsShuttingdown() {
        assertEquals(RepeaterServiceState.shuttingdown, repeater.getState());
        //todo: controleren dat er minimaal 1 actieve thread is
    }

    public void assertDesiredPoolSize(int expected) {
        assertEquals(expected, repeater.getDesiredPoolSize());
    }

    public void assertActualPoolSize(int expected) {
        assertEquals(expected, repeater.getActualPoolSize());
    }

    public void assertHasRepeatable(Repeatable task) {
        assertSame(repeater.getLendableRef().peek(), task);
    }

    public AwaitShutdownThread scheduleAwaitShutdown() {
        AwaitShutdownThread t = new AwaitShutdownThread();
        t.start();
        return t;
    }

    public RepeatThread scheduleRepeat(Repeatable task) {
        RepeatThread t = new RepeatThread(task);
        t.start();
        return t;
    }

    public AwaitShutdownThread scheduleAwaitSchutdown() {
        AwaitShutdownThread t = new AwaitShutdownThread();
        t.start();
        return t;
    }

    public class RepeatThread extends TestThread {

        private final Repeatable task;

        public RepeatThread(Repeatable task) {
            this.task = task;
        }

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            repeater.repeat(task);
        }
    }

    public ShutdownThread scheduleShutdown() {
        ShutdownThread t = new ShutdownThread();
        t.start();
        return t;
    }

    public class ShutdownThread extends TestThread {
        protected void runInternal() throws Exception {
            repeater.shutdown();
        }
    }

    public TryRepeatThread scheduleTryRepeat(Repeatable task, boolean startInterrupted) {
        TryRepeatThread t = new TryRepeatThread(task);
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public class TryRepeatThread extends TestThread {
        private final Repeatable task;
        private volatile boolean success;

        public TryRepeatThread(Repeatable task) {
            this.task = task;
        }

        @Override
        public void runInternal() {
            success = repeater.tryRepeat(task);
        }

        public void assertSuccess() {
            assertIsTerminatedNormally();
            assertTrue(success);
        }

        public void assertFailed() {
            assertIsTerminatedNormally();
            assertFalse(success);
        }
    }

    public TimedTryRepeatThread scheduleTimedTryRepeat(Repeatable task, long timeoutMs) {
        TimedTryRepeatThread t = new TimedTryRepeatThread(task, timeoutMs);
        t.start();
        return t;
    }

    public ShutdownNowThread scheduleShutdownNow() {
        ShutdownNowThread t = new ShutdownNowThread();
        t.start();
        return t;
    }

    public class TimedTryRepeatThread extends TestThread {

        private final long timeoutMs;
        private final Repeatable task;

        public TimedTryRepeatThread(Repeatable task, long timeoutMs) {
            this.task = task;
            this.timeoutMs = timeoutMs;
        }

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            repeater.tryRepeat(task, timeoutMs, TimeUnit.MILLISECONDS);
        }

        public void assertSuccess() {
            assertIsTerminatedNormally();
        }
    }

    public class ShutdownNowThread extends TestThread {
        @Override
        protected void runInternal() {
            repeater.shutdownNow();
        }
    }

    public class AwaitShutdownThread extends TestThread {
        @Override
        protected void runInternal() throws InterruptedException {
            repeater.awaitShutdown();
        }
    }
}
