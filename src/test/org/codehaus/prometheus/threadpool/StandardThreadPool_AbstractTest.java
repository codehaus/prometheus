/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.concurrenttesting.*;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.joinAll;
import static org.codehaus.prometheus.concurrenttesting.TestSupport.newSleepingRunnable;
import org.codehaus.prometheus.exceptionhandler.TracingExceptionHandler;
import org.codehaus.prometheus.util.StandardThreadFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Peter Veentjer.
 */
public abstract class StandardThreadPool_AbstractTest extends ConcurrentTestCase {

    protected volatile StandardThreadPool threadpool;
    protected volatile TracingThreadFactory threadPoolThreadFactory;
    protected volatile BlockingQueue<Callable<Boolean>> workQueue;
    protected volatile TracingExceptionHandler threadPoolExceptionHandler;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        workQueue = new LinkedBlockingQueue<Callable<Boolean>>();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        if (threadpool == null)
            return;

        TestThread t = new TestThread() {
            @Override
            protected void runInternal() throws Exception {
                threadpool.shutdownNow();
                threadpool.awaitShutdown();
            }
        };
        t.start();
        joinAll(t);
        t.assertIsTerminatedNormally();
        assertIsShutdown();
    }

    public void spawned_start() {
        StartThread t = scheduleStart();
        joinAll(t);
        t.assertIsTerminatedNormally();
    }

    public void spawned_shutdown() {
        ShutdownThread t = scheduleShutdown();
        joinAll(t);
        t.assertIsTerminatedNormally();
    }

    public void spawnNewWorker(){
        threadpool.spawn(new TestThreadPoolJob());
    }

    public void spawned_shutdownNow() {
        ShutdownNowThread t = scheduleShutdownNow();
        joinAll(t);
        t.assertIsTerminatedNormally();
    }

    public List<TestRunnable> ensureNoIdleWorkers(long delayMs) {
        return ensureNoIdleWorkers(delayMs, true);
    }

    //all workers are going to execute a task that takes an eon to complete
    public List<TestRunnable> ensureNoIdleWorkers() {
        return ensureNoIdleWorkers(Delays.EON_MS, true);
    }

    /**
     * Ensure that all workers are working
     *
     * @param delayMs the duration of the task
     */
    public List<TestRunnable> ensureNoIdleWorkers(long delayMs, boolean interruptable) {

        try {
            List<TestRunnable> list = new LinkedList<TestRunnable>();
            for (int k = 0; k < threadpool.getActualPoolSize(); k++) {
                TestRunnable task = newSleepingRunnable(delayMs, interruptable);
                list.add(task);
                workQueue.put(Executors.callable(task, true));
            }
            giveOthersAChance();
            //make sure that all workers are executing a job.
            assertTrue(workQueue.isEmpty());
            return list;
        } catch (InterruptedException ex) {
            fail("unexpected interrupted exception");
            throw new RuntimeException("can't happen");
        }
    }

    public void newStartedThreadpool() {
        newUnstartedThreadPool();
        threadpool.start();
    }

    public void newStartedThreadpool(int poolsize) {
        newUnstartedThreadPool();
        threadpool.start();
        threadpool.spawn(new TestThreadPoolJob(), poolsize);
    }

    public List<TestRunnable> newShuttingdownThreadpool(int poolsize, long runningTimeMs) {
        return newShuttingdownThreadpool(poolsize, runningTimeMs, true);
    }

    public List<TestRunnable> newShuttingdownThreadpool(int poolsize, long runningTimeMs, boolean interruptible) {
        newStartedThreadpool(poolsize);

        List<TestRunnable> list = ensureNoIdleWorkers(runningTimeMs, interruptible);

        //shut down the threadpool
        spawned_shutdown();
        assertIsShuttingdown();
        return list;
    }

    public List<TestRunnable> newForcedShuttingdownThreadpool(int poolsize, long runningTimeMs) {
        newStartedThreadpool(poolsize);

        List<TestRunnable> list = ensureNoIdleWorkers(runningTimeMs, false);

        spawned_shutdownNow();
        assertIsForcedShuttingdown();
        return list;
    }

    public void newUnstartedThreadPool() {
        threadPoolThreadFactory = new TracingThreadFactory(new StandardThreadFactory());
        threadpool = new StandardThreadPool(threadPoolThreadFactory);
        threadPoolExceptionHandler = new TracingExceptionHandler();
        threadpool.setExceptionHandler(threadPoolExceptionHandler);
    }

    public void newUnstartedThreadPool(int poolsize) {
        newUnstartedThreadPool();
        threadpool.spawnWithoutStarting(new TestThreadPoolJob(), poolsize);
    }

    public void newShutdownThreadpool() {
        newStartedThreadpool();
        spawned_shutdown();
    }

    public void assertIsRunning() {
        assertHasState(ThreadPoolState.running);
    }

    public void assertIsUnstarted() {
        assertHasState(ThreadPoolState.unstarted);
    }

    public void assertIsShutdown() {
        assertHasState(ThreadPoolState.shutdown);
        assertEquals(0, threadpool.getActualPoolSize());
        if (threadPoolThreadFactory != null)
            threadPoolThreadFactory.assertAllAreNotAlive();
    }

    public void assertIsForcedShuttingdown() {
        assertHasState(ThreadPoolState.shuttingdownforced);
    }

    public void assertIsShuttingdown() {
        assertHasState(ThreadPoolState.shuttingdownnormally);
    }

    public void assertHasState(ThreadPoolState expectedState) {
        assertEquals(expectedState, threadpool.getState());
    }


    public void assertDesiredPoolsize(int expectedPoolsize) {
        // assertEquals(expectedPoolsize, threadpool.getDesiredPoolSize());
        // throw new RuntimeException();
    }

    public void assertActualPoolsize(int expectedPoolsize) {
        assertEquals(expectedPoolsize, threadpool.getActualPoolSize());
    }

    public void assertWorkQueueIsEmpty() {
        assertTrue(workQueue.isEmpty());
    }

    public ShutdownNowThread scheduleShutdownNow() {
        ShutdownNowThread t = new ShutdownNowThread();
        t.start();
        return t;
    }

    public ShutdownThread scheduleShutdown() {
        ShutdownThread t = new ShutdownThread();
        t.start();
        return t;
    }

    public SetDesiredPoolsizeThread scheduleSetDesiredPoolsize(int poolsize) {
        SetDesiredPoolsizeThread t = new SetDesiredPoolsizeThread(poolsize);
        t.start();
        return t;
    }

    public StartThread scheduleStart() {
        StartThread t = new StartThread();
        t.start();
        return t;
    }

    public AwaitShutdownThread scheduleAwaitShutdown() {
        AwaitShutdownThread t = new AwaitShutdownThread();
        t.start();
        return t;
    }

    public TryAwaitShutdownThread scheduleTryAwaitShutdown(long timeoutMs) {
        TryAwaitShutdownThread t = new TryAwaitShutdownThread(timeoutMs);
        t.start();
        return t;
    }

    public SetDefaultWorkerJobThread scheduleSetDefaultWorkerJob(ThreadPoolJob threadPoolJob) {
        SetDefaultWorkerJobThread t = new SetDefaultWorkerJobThread(threadPoolJob);
        t.start();
        return t;
    }

    public class SetDesiredPoolsizeThread extends TestThread {
        private final int poolsize;

        public SetDesiredPoolsizeThread(int poolsize) {
            this.poolsize = poolsize;
        }

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            //threadpool.setDesiredPoolsize(poolsize);
            throw new RuntimeException();
        }
    }

    public class StartThread extends TestThread {
        @Override
        protected void runInternal() {
            threadpool.start();
        }
    }

    public class ShutdownNowThread extends TestThread {
        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            threadpool.shutdownNow();
        }
    }

    public class ShutdownThread extends TestThread {
        @Override
        protected void runInternal() {
            threadpool.shutdownPolitly();
        }
    }

    public class SetDefaultWorkerJobThread extends TestThread {
        private final ThreadPoolJob threadPoolJob;

        public SetDefaultWorkerJobThread(ThreadPoolJob threadPoolJob) {
            this.threadPoolJob = threadPoolJob;
        }

        @Override
        protected void runInternal() throws Exception {
            //threadpool.setJob(threadPoolJob);
            throw new RuntimeException();
        }
    }

    public class TryAwaitShutdownThread extends TestThread {
        final long timeoutMs;

        public TryAwaitShutdownThread(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        @Override
        public void runInternal() throws TimeoutException, InterruptedException {
            threadpool.tryAwaitShutdown(timeoutMs, TimeUnit.MILLISECONDS);
        }
    }

    public class AwaitShutdownThread extends TestThread {
        @Override
        protected void runInternal() throws InterruptedException {
            threadpool.awaitShutdown();
        }
    }

    /**
     * A ThreadPoolJob that takes work from the taskQueue to execute.
     */
    public class TestThreadPoolJob implements ThreadPoolJob<Callable<Boolean>> {

        public Callable<Boolean> takeWork() throws InterruptedException {
            if (threadpool.getState() == ThreadPoolState.running)
                return workQueue.take();
            else
                return workQueue.poll();
        }

        public boolean executeWork(Callable<Boolean> task) throws Exception {
            return task == null ? false : task.call();
        }
    }
}
