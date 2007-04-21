/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.testsupport.NonInterruptableSleepingRunnable;
import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.util.StandardThreadFactory;

import java.util.concurrent.*;

public class ThreadPoolBlockingExecutor_AbstractTest extends ConcurrentTestCase {

    public volatile ThreadPoolBlockingExecutor executor;

    public void tearDown() {
        if (executor != null)
            executor.shutdown();
    }

    public void newStartedBlockingExecutor(int queuesize, int poolsize) {
        newUnstartedBlockingExecutor(queuesize,poolsize);
        executor.start();
    }

    public void newStartedBlockingExecutor() {
       newShutdownBlockingExecutor(1,1);
    }


    public void newStartedBlockingExecutor(int queuesize, int poolsize,Runnable initialTask) {
        newStartedBlockingExecutor(queuesize,poolsize);
        try {
            executor.execute(initialTask);
            Thread.yield();
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void newUnstartedBlockingExecutor(int queuesize, int poolsize) {
        executor = new ThreadPoolBlockingExecutor(
                        new StandardThreadFactory(),
                        poolsize,
                        new LinkedBlockingQueue<Runnable>(queuesize));
    }

    public void newShutdownBlockingExecutor(int queuesize,int poolsize) {
        newUnstartedBlockingExecutor(queuesize,poolsize);
        executor.shutdown();
    }

    public void newShuttingDownBlockingExecutor(long timeMs) {
        newStartedBlockingExecutor(1,1,new NonInterruptableSleepingRunnable(timeMs));
        executor.shutdown();
    }

    public void assertDesiredPoolSize(int expectedPoolSize) {
        assertEquals(expectedPoolSize, executor.getDesiredPoolSize());
    }

    public void assertActualPoolSize(int expectedActualPoolSize) {
        assertEquals(expectedActualPoolSize, executor.getActualPoolSize());
    }

    public void assertIsUnstarted() {
        assertEquals(BlockingExecutorServiceState.Unstarted, executor.getState());
    }

    public void assertIsRunning() {
        assertEquals(BlockingExecutorServiceState.Running, executor.getState());
    }

    public void assertIsShuttingDown() {
        assertEquals(BlockingExecutorServiceState.Shuttingdown, executor.getState());
    }

    public void assertIsShutdown() {
        assertEquals(BlockingExecutorServiceState.Shutdown, executor.getState());
    }

    public void assertWorkQueueIsEmpty(){
        assertTrue(executor.getWorkQueue().isEmpty());
    }

    public void assertTasksOnWorkQueue(Runnable... expected) {
        BlockingQueue workQueue = executor.getWorkQueue();
        Runnable[] foundTasks = (Runnable[]) workQueue.toArray();
        assertEquals(expected.length, foundTasks.length);
        for (int k = 0; k < expected.length; k++) {
            Runnable foundTask = foundTasks[k];
            Runnable expectedTask = expected[k];
            assertSame(expectedTask, foundTask);
        }
    }

    public TryAwaitShutdownThread scheduleTryAwaitShutdown(long delayMs) {
        TryAwaitShutdownThread t = new TryAwaitShutdownThread(delayMs, TimeUnit.MILLISECONDS);
        t.start();
        return t;
    }

    public AwaitShutdownThread scheduleAwaitShutdown(){
       AwaitShutdownThread t = new AwaitShutdownThread();
        t.start();
        return t;
    }

    public ShutdownThread scheduleShutdown(){
        ShutdownThread t = new ShutdownThread();
        t.start();
        return t;
    }

    public ExecuteThread scheduleExecute(Runnable task, boolean startInterrupted){
        ExecuteThread t = new ExecuteThread(task);
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public class ExecuteThread extends TestThread {
        private final Runnable task;

        public ExecuteThread(Runnable task) {
            this.task = task;
        }

        protected void runInternal() throws InterruptedException {
            executor.execute(task);
        }
    }

    public class TryAwaitShutdownThread extends TestThread {
        private final long timeout;
        private final TimeUnit timeoutUnit;

        public TryAwaitShutdownThread(long timeout, TimeUnit timeoutUnit) {
            this.timeout = timeout;
            this.timeoutUnit = timeoutUnit;
        }

        protected void runInternal() throws InterruptedException, TimeoutException {
            executor.tryAwaitShutdown(timeout, timeoutUnit);
        }

        public void assertSuccess() {
            assertIsTerminated();
        }
    }

    public class ShutdownThread extends TestThread {
        protected void runInternal() throws InterruptedException, TimeoutException {
           executor.shutdown();
        }
    }

    public class AwaitShutdownThread extends TestThread {

        protected void runInternal() throws InterruptedException, TimeoutException {
            executor.awaitShutdown();
        }
    }
}
