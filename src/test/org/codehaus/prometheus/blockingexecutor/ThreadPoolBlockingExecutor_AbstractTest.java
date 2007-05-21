/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.testsupport.SleepingRunnable;
import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.testsupport.TracingThreadFactory;
import org.codehaus.prometheus.util.StandardThreadFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Peter Veentjer
 */
public abstract class ThreadPoolBlockingExecutor_AbstractTest extends ConcurrentTestCase {

    public volatile ThreadPoolBlockingExecutor executor;
    public volatile TracingThreadFactory threadFactory;

    public void tearDown() throws Exception {
        super.tearDown();

        if (executor != null) {
            ShutdownNowThread shutdownNowThread = scheduleShutdownNow();
            joinAll(shutdownNowThread);
            shutdownNowThread.assertIsTerminatedWithoutThrowing();

            AwaitShutdownThread awaitThread = scheduleAwaitShutdown();
            joinAll(awaitThread);
            awaitThread.assertIsTerminatedWithoutThrowing();

            assertIsShutdown();
        }
    }

    public ThreadPoolBlockingExecutor getExecutor() {
        return executor;
    }

    public TracingThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public void newStartedBlockingExecutor(int queuesize, int poolsize) {
        newUnstartedBlockingExecutor(queuesize, poolsize);
        executor.start();
    }

    public void newStartedBlockingExecutor() {
        newStartedBlockingExecutor(1,0);
    }

    public void newStartedBlockingExecutor(int queuesize, int poolsize, Runnable initialTask) {
        newStartedBlockingExecutor(queuesize, poolsize);
        try {
            executor.execute(initialTask);
            giveOthersAChance();
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void newUnstartedBlockingExecutor(int queuesize, int poolsize) {
        BlockingQueue<Runnable> queue = queuesize == 0 ? new SynchronousQueue() : new LinkedBlockingQueue();
        threadFactory = new TracingThreadFactory(new StandardThreadFactory());
        executor = new ThreadPoolBlockingExecutor(
                poolsize,
                threadFactory,
                queue);
    }

    public void newShutdownBlockingExecutor(int queuesize, int poolsize) {
        newUnstartedBlockingExecutor(queuesize, poolsize);
        executor.shutdown();
    }

    public void newShuttingDownBlockingExecutor(long timeMs) {
        newStartedBlockingExecutor(1, 1, new SleepingRunnable(timeMs));
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

        //it could be that no reference to the threadfactory was assigned
        if (threadFactory != null)
            threadFactory.assertThreadsHaveTerminated();

        assertActualPoolSize(0);
        assertWorkQueueIsEmpty();
    }

    public void assertWorkQueueIsEmpty() {
        assertTrue(executor.getWorkQueue().isEmpty());
    }

    public void assertTasksOnWorkQueue(Runnable... expected) {
        BlockingQueue workQueue = executor.getWorkQueue();
        Runnable[] foundTasks = (Runnable[]) workQueue.toArray(new Runnable[]{});
        assertEquals(expected.length, foundTasks.length);
        for (int k = 0; k < expected.length; k++) {
            Runnable foundTask = foundTasks[k];
            Runnable expectedTask = expected[k];
            assertSame(expectedTask, foundTask);
        }
    }

    public void executeEonTask(int count) {
        for (int k = 0; k < count; k++)
            executeEonTask();
    }

    public SleepingRunnable executeEonTask() {
        SleepingRunnable sleepingRunnable = new SleepingRunnable(DELAY_EON_MS);
        try {
            executor.execute(sleepingRunnable);
        } catch (InterruptedException ex) {
            fail();
        }
        return sleepingRunnable;
    }

    public TryAwaitShutdownThread scheduleTryAwaitShutdown(long timeoutMs) {
        TryAwaitShutdownThread t = new TryAwaitShutdownThread(timeoutMs);
        t.start();
        return t;
    }

    public TryAwaitShutdownThread scheduleTryAwaitShutdown(long timeoutMs, boolean startInterrupted) {
        TryAwaitShutdownThread t = new TryAwaitShutdownThread(timeoutMs);
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public TryExecuteThread scheduleTryExecute(Runnable task, long timeoutMs) {
        TryExecuteThread t = new TryExecuteThread(task, timeoutMs);
        t.start();
        return t;
    }

    public AwaitShutdownThread scheduleAwaitShutdown() {
        AwaitShutdownThread t = new AwaitShutdownThread();
        t.start();
        return t;
    }

    public StartThread scheduleStart() {
        StartThread t = new StartThread();
        t.start();
        return t;
    }

    public ShutdownThread scheduleShutdown() {
        ShutdownThread t = new ShutdownThread();
        t.start();
        return t;
    }

    public ShutdownNowThread scheduleShutdownNow() {
        ShutdownNowThread t = new ShutdownNowThread();
        t.start();
        return t;
    }

    public ExecuteThread scheduleExecute(Runnable task, boolean startInterrupted) {
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

        @Override
        protected void runInternal() throws InterruptedException {
            executor.execute(task);
        }
    }

    public class TryAwaitShutdownThread extends TestThread {
        private final long timeoutMs;

        public TryAwaitShutdownThread(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            executor.tryAwaitShutdown(timeoutMs, TimeUnit.MILLISECONDS);
        }

        public void assertSuccess() {
            assertIsTerminatedWithoutThrowing();
        }
    }

    public class ShutdownThread extends TestThread {

        @Override
        protected void runInternal() {
            executor.shutdown();
        }
    }

    public class ShutdownNowThread extends TestThread {
        private volatile List<Runnable> foundTask;

        @Override
        protected void runInternal() throws Exception {
            foundTask = executor.shutdownNow();
        }

        public void assertSuccess(Runnable... expectedTasks) {
            assertIsTerminatedWithoutThrowing();
            List<Runnable> list = Arrays.asList(expectedTasks);
            assertEquals(list, foundTask);
        }
    }

    public class AwaitShutdownThread extends TestThread {

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            executor.awaitShutdown();
        }
    }

    public class StartThread extends TestThread {

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            executor.start();
        }
    }


    public class TryExecuteThread extends TestThread {

        private final Runnable task;
        private final long timeoutMs;

        public TryExecuteThread(Runnable task, long timeoutMs) {
            this.task = task;
            this.timeoutMs = timeoutMs;
        }

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            executor.tryExecute(task, timeoutMs, TimeUnit.MILLISECONDS);
        }

        public void assertIsSuccess() {
            assertIsTerminatedWithoutThrowing();
        }

        public void assertIsRejected() {
            assertIsTerminatedWithThrowing(RejectedExecutionException.class);
        }
    }
}
