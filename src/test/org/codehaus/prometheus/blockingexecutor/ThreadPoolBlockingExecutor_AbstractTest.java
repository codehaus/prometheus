/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.concurrenttesting.*;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.joinAll;
import static org.codehaus.prometheus.concurrenttesting.TestSupport.*;
import org.codehaus.prometheus.util.StandardThreadFactory;

import java.util.Arrays;
import static java.util.Arrays.asList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Peter Veentjer
 */
public abstract class ThreadPoolBlockingExecutor_AbstractTest extends ConcurrentTestCase {

    public volatile ThreadPoolBlockingExecutor executor;
    public volatile TracingThreadFactory threadFactory;

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        if (executor != null) {
            spawned_assertShutdownNow();
            spawned_assertAwaitShutdown();
        }
    }

    public SleepingRunnable spawned_placeSleepingTask(long durationMs) {
        SleepingRunnable task = newSleepingRunnable(durationMs);
        ExecuteThread executeThread = scheduleExecute(task, START_UNINTERRUPTED);
        joinAll(executeThread);
        executeThread.assertIsTerminatedNormally();
        return task;
    }

    public void spawned_assertAwaitShutdown() {
        AwaitShutdownThread t = scheduleAwaitShutdown();
        joinAll(t);
        t.assertIsTerminatedNormally();
        assertIsShutdown();
    }

    public void spawned_assertShutdownNow() {
        ShutdownNowThread t = scheduleShutdownNow();
        joinAll(t);
        t.assertIsTerminatedNormally();
    }

    public void spawned_shutdownPolitly(Runnable... expectedOutstandingTasks) {
        ShutdownPolitlyThread t = scheduleShutdownPolitly();
        joinAll(t);
        t.assertSuccess(expectedOutstandingTasks);
        assertIsShuttingDownOrShutdown();
    }

    public void spawned_start() {
        StartThread t = scheduleStart();
        joinAll(t);
        t.assertIsTerminatedNormally();
    }

    public void executeUninterruptibleSleepingTasks(long sleepMs, int count) {
        List<TestRunnable> tasks = newUninterruptibleSleepingRunnables(sleepMs, count);
        for (TestRunnable task : tasks)
            spawned_execute(task);
    }

    public void spawned_execute(Runnable... tasks) {
        for (Runnable task : tasks) {
            ExecuteThread t = scheduleExecute(task);
            joinAll(t);
            t.assertIsTerminatedNormally();
        }
    }

    public void spawned_assertSetDesiredPoolSize(int poolsize) {
        SetDesiredPoolSizeThread t = scheduleSetDesiredPoolSize(poolsize);
        joinAll(t);
        t.assertIsTerminatedNormally();
    }

    public void spawned_assertSetDesiredPoolSizeThrowsException(int poolsize, Class exClass) {
        SetDesiredPoolSizeThread t = scheduleSetDesiredPoolSize(poolsize);
        joinAll(t);
        t.assertIsTerminatedWithThrowing(exClass);
    }

    public void newStartedBlockingExecutor(int queuesize, int poolsize) {
        newUnstartedBlockingExecutor(queuesize, poolsize);
        executor.start();
    }

    public void newStartedBlockingExecutor() {
        newStartedBlockingExecutor(1, 0);
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

    public void newShutdownBlockingExecutor() {
        newUnstartedBlockingExecutor(0, 0);
        executor.shutdownPolitly();
    }

    public void newShutdownBlockingExecutor(int queuesize, int poolsize) {
        newUnstartedBlockingExecutor(queuesize, poolsize);
        executor.shutdownPolitly();
    }

    public void newForcedShuttingdownBlockingExecutor(long sleepMs, int poolsize) {
        newStartedBlockingExecutor(10000, poolsize);
        executeUninterruptibleSleepingTasks(sleepMs, poolsize);
        giveOthersAChance();
        assertWorkQueueContains();
        spawned_assertShutdownNow();
        assertIsShuttingdown();
    }

    public void newShuttingdownBlockingExecutor(long timeMs) {
        newStartedBlockingExecutor(1, 1, newSleepingRunnable(timeMs));
        executor.shutdownPolitly();
    }

    public void assertDesiredPoolSize(int expectedPoolSize) {
        assertEquals(expectedPoolSize, executor.getDesiredPoolSize());
    }

    public void assertActualPoolSize(int expectedActualPoolSize) {
        assertEquals(expectedActualPoolSize, executor.getActualPoolSize());
    }

    public void assertIsUnstarted() {
        assertHasState(BlockingExecutorServiceState.Unstarted);
    }

    public void assertIsRunning() {
        assertHasState(BlockingExecutorServiceState.Running);
    }

    public void assertIsShuttingdown() {
        assertHasState(BlockingExecutorServiceState.Shuttingdown);
    }

    public void assertIsShutdown() {
        assertHasState(BlockingExecutorServiceState.Shutdown);

        //it could be that no reference to the threadfactory was assigned
        if (threadFactory != null)
            threadFactory.assertAllAreNotAlive();

        assertActualPoolSize(0);
        assertWorkQueueIsEmpty();
    }

    public void assertHasState(BlockingExecutorServiceState expected) {
        assertEquals(expected, executor.getState());
    }

    public void assertWorkQueueIsEmpty() {
        assertTrue(executor.getWorkQueue().isEmpty());
    }

    public void assertWorkQueueContains(Runnable... expected) {
        List<Runnable> workList = new LinkedList<Runnable>(executor.getWorkQueue());
        List<Runnable> expectedList = asList(expected);
        assertEquals(expectedList, workList);
    }

    public ShutdownNowThread spawned_assertShutdownNow(Runnable... expectedUnprocessed) {
        ShutdownNowThread shutdownThread = scheduleShutdownNow();
        joinAll(shutdownThread);
        shutdownThread.assertSuccess(expectedUnprocessed);
        assertIsShuttingDownOrShutdown();
        assertWorkQueueIsEmpty();
        return shutdownThread;
    }

    public void assertIsShuttingDownOrShutdown() {
        BlockingExecutorServiceState state = executor.getState();
        assertTrue(String.format("state was %s", state),
                state == BlockingExecutorServiceState.Shuttingdown || state == BlockingExecutorServiceState.Shutdown);
    }

    public void executeEonTask(int count) {
        for (int k = 0; k < count; k++)
            executeEonTask();
    }

    public SleepingRunnable executeEonTask() {
        SleepingRunnable sleepingRunnable = newEonSleepingRunnable();
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

    public ShutdownPolitlyThread scheduleShutdownPolitly() {
        ShutdownPolitlyThread t = new ShutdownPolitlyThread();
        t.start();
        return t;
    }

    public ShutdownNowThread scheduleShutdownNow() {
        ShutdownNowThread t = new ShutdownNowThread();
        t.start();
        return t;
    }

    public ExecuteThread scheduleExecute(Runnable task) {
        return scheduleExecute(task, START_UNINTERRUPTED);
    }

    public ExecuteThread scheduleExecute(Runnable task, boolean startInterrupted) {
        ExecuteThread t = new ExecuteThread(task);
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public SetDesiredPoolSizeThread scheduleSetDesiredPoolSize(int poolsize) {
        SetDesiredPoolSizeThread t = new SetDesiredPoolSizeThread(poolsize);
        t.start();
        return t;
    }

    public class SetDesiredPoolSizeThread extends TestThread {
        final int size;

        public SetDesiredPoolSizeThread(int size) {
            this.size = size;
        }

        @Override
        protected void runInternal() throws Exception {
            executor.setDesiredPoolSize(size);
        }
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
            assertIsTerminatedNormally();
        }
    }

    public class ShutdownPolitlyThread extends TestThread {
        private volatile List<Runnable> foundTasks;

        @Override
        protected void runInternal() {
            foundTasks = executor.shutdownPolitly();
        }

        public void assertSuccess(Runnable... expectedTasks) {
            assertIsTerminatedNormally();
            List<Runnable> list = Arrays.asList(expectedTasks);
            assertEquals(list, foundTasks);
        }
    }

    public class ShutdownNowThread extends TestThread {
        private volatile List<Runnable> foundTasks;

        @Override
        protected void runInternal() throws Exception {
            foundTasks = executor.shutdownNow();
        }

        public void assertSuccess(Runnable... expectedTasks) {
            assertIsTerminatedNormally();
            List<Runnable> list = Arrays.asList(expectedTasks);
            assertEquals(list, foundTasks);
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
            assertIsTerminatedNormally();
        }

        public void assertIsRejected() {
            assertIsTerminatedWithThrowing(RejectedExecutionException.class);
        }
    }
}
