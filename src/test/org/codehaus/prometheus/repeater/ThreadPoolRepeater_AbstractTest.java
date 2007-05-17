/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.testsupport.NonInterruptableSleepingRunnable;
import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.util.StandardThreadFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class ThreadPoolRepeater_AbstractTest extends ConcurrentTestCase {

    public volatile ThreadPoolRepeater repeater;


    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        //if a repeater is available, make sure that is can shut down
        if (repeater != null) {
            ShutdownNowThread shutdownNowThread = scheduleShutdownNow();
            joinAll(shutdownNowThread);

            AwaitShutdownThread awaitShutdownThread = scheduleAwaitShutdown();
            joinAll(awaitShutdownThread);
            assertIsShutdown();
        }
    }

    public void newShutdownRepeater() {
        newRunningStrictRepeater();
        repeater.shutdownNow();
        try {
            repeater.awaitShutdown();
        } catch (InterruptedException e) {
            fail();
        }
        assertIsShutdown();
    }

    public void newShutdownRepeater(boolean strict) {
        newRunningRepeater(strict);
        repeater.shutdownNow();
        try {
            repeater.awaitShutdown();
        } catch (InterruptedException e) {
            fail();
        }
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
        newUnstartedRepeater(strict,poolsize);
        repeater.start();
        assertIsRunning();
    }

    public void newRunningRepeater(boolean strict, Repeatable task) {
        newUnstartedRepeater(strict);
        try {
            repeater.repeat(task);
            //give the worker in the threadpool time to start the task
            //If this yield is removed, the thread is started, but maybe has not entered his
            //runWork method. If the shutdownNow is called before the runWork method
            //has runWork, the worker thread sees that the repeater is shutting down,
            //so the task is not going to be executed, and this is not
            //what we want.
            Thread.yield();
        } catch (InterruptedException e) {
            fail();
        }
        assertIsRunning();
    }

    public void newRunningStrictRepeater(Repeatable task) {
        newRunningRepeater(true, task);
    }

    public void newUnstartedStrictRepeater() {
        repeater = new ThreadPoolRepeater(1);
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
        repeater = new ThreadPoolRepeater(strict, null, poolsize, new StandardThreadFactory());
        assertIsUnstarted();
    }

    public void newShuttingdownRepeater(long timeMs) {
        newRunningStrictRepeater(new RepeatableRunnable(new NonInterruptableSleepingRunnable(timeMs)));
        repeater.shutdownNow();
        assertIsShuttingdown();
    }

    public void newShuttingdownRepeater(boolean strict, long timeMs) {
        newRunningRepeater(strict, new RepeatableRunnable(new NonInterruptableSleepingRunnable(timeMs)));
        repeater.shutdownNow();
        assertIsShuttingdown();
    }

    public void tested_repeat(Runnable task){
        tested_repeat((Repeatable)new RepeatableRunnable(task));
    }

    public void tested_repeat(Repeatable task){
        RepeatThread repeatThread = scheduleRepeat(task);
        joinAll(repeatThread);
    }

    public void assertIsUnstarted() {
        assertEquals(RepeaterServiceState.Unstarted, repeater.getState());
    }

    public void assertIsShutdown() {
        assertEquals(RepeaterServiceState.Shutdown, repeater.getState());
    }

    public void assertIsRunning() {
        assertEquals(RepeaterServiceState.Running, repeater.getState());
    }

    public void assertIsShuttingdown() {
        assertEquals(RepeaterServiceState.Shuttingdown, repeater.getState());
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

    public AwaitShutdownThread scheduleAwaitShutdown(){
        AwaitShutdownThread t = new AwaitShutdownThread();
        t.start();
        return t;
    }

    public RepeatThread scheduleRepeat(Repeatable task) {
        RepeatThread t = new RepeatThread(task);
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
            assertIsTerminatedWithoutThrowing();
            assertTrue(success);
        }

        public void assertFailure() {
            assertIsTerminatedWithoutThrowing();
            assertFalse(success);
        }
    }

    public TimedTryRepeatThread scheduleTimedTryRepeat(Repeatable task, long timeoutMs) {
        TimedTryRepeatThread t = new TimedTryRepeatThread(task, timeoutMs);
        t.start();
        return t;
    }

    public ShutdownNowThread scheduleShutdownNow(){
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
            assertIsTerminatedWithoutThrowing();
        }
    }

    public class ShutdownNowThread extends TestThread{
        @Override
        protected void runInternal(){
            repeater.shutdownNow();
        }
    }

    public class AwaitShutdownThread extends TestThread{
        @Override
        protected void runInternal() throws InterruptedException {
            repeater.awaitShutdown();
        }
    }
}
