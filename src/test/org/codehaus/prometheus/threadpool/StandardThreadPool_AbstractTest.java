/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.exceptionhandler.TracingExceptionHandler;
import org.codehaus.prometheus.testsupport.*;
import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;
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

    public void spawned_shutdownNow() {
        ShutdownNowThread t = scheduleShutdownNow();
        joinAll(t);
        t.assertIsTerminatedNormally();
    }

    /**
     * A ThreadPoolJob that takes work from the taskQueue to execute.
     */
    public class TestThreadPoolJob implements ThreadPoolJob<Callable<Boolean>> {

        public Callable<Boolean> getWork() throws InterruptedException {
            return workQueue.take();
        }

        public Callable<Boolean> getShuttingdownWork() throws InterruptedException {
            return workQueue.poll(0, TimeUnit.MILLISECONDS);
        }

        public boolean executeWork(Callable<Boolean> task) throws Exception {
            return task.call();
        }
    }

    public List<TestRunnable> ensureNoIdleWorkers(long delayMs) {
        return ensureNoIdleWorkers(delayMs, true);
    }

    //all workers are going to execute a task that takes an eon to complete
    public List<TestRunnable> ensureNoIdleWorkers() {
        return ensureNoIdleWorkers(DELAY_EON_MS, true);
    }

    /**
     * Ensure that all workers are working
     *
     * @param delayMs the duration of the task
     */
    public List<TestRunnable> ensureNoIdleWorkers(long delayMs, boolean interruptable) {
        try {
            List<TestRunnable> list = new LinkedList<TestRunnable>();
            for (int k = 0; k < threadpool.getDesiredPoolSize(); k++) {
                TestRunnable task = interruptable ? new SleepingRunnable(delayMs) : new UninterruptableSleepingRunnable(delayMs);
                list.add(task);
                workQueue.put(Executors.callable(task,true));
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
        newUnstartedThreadPool(poolsize);
        threadpool.start();
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
        newUnstartedThreadPool(0);
    }

    public void newUnstartedThreadPoolWithoutDefaultJob(int poolsize) {
        threadPoolThreadFactory = new TracingThreadFactory(new StandardThreadFactory());
        threadpool = new StandardThreadPool(threadPoolThreadFactory);
        threadpool.setDesiredPoolsize(poolsize);
        threadPoolExceptionHandler = new TracingExceptionHandler();
        threadpool.setExceptionHandler(threadPoolExceptionHandler);
    }

    public void newUnstartedThreadPool(int poolsize) {
        newUnstartedThreadPoolWithoutDefaultJob(poolsize);
        threadpool.setJob(new TestThreadPoolJob());
    }

    public void newShutdownThreadpool() {
        newStartedThreadpool();
        spawned_shutdown();
    }

    public void assertIsRunning() {
        assertEquals(ThreadPoolState.running, threadpool.getState());
    }

    public void assertIsUnstarted() {
        assertEquals(ThreadPoolState.unstarted, threadpool.getState());
    }

    public void assertIsShutdown() {
        assertEquals(ThreadPoolState.shutdown, threadpool.getState());
        assertEquals(0, threadpool.getActualPoolSize());
        if (threadPoolThreadFactory != null)
            threadPoolThreadFactory.assertAllThreadsAreTerminated();
    }

    public void assertIsForcedShuttingdown() {
        assertEquals(ThreadPoolState.forcedshuttingdown, threadpool.getState());
    }

    public void assertIsShuttingdown() {
        assertEquals(ThreadPoolState.shuttingdown, threadpool.getState());
    }

    public void assertDesiredPoolsize(int expectedPoolsize) {
        assertEquals(expectedPoolsize, threadpool.getDesiredPoolSize());
    }

    public void assertActualPoolsize(int expectedPoolsize) {
        assertEquals(expectedPoolsize, threadpool.getActualPoolSize());
    }

    public void assertWorkQueueIsEmpty(){
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
            threadpool.setDesiredPoolsize(poolsize);
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
            threadpool.shutdown();
        }
    }

    public class SetDefaultWorkerJobThread extends TestThread {
        private final ThreadPoolJob threadPoolJob;

        public SetDefaultWorkerJobThread(ThreadPoolJob threadPoolJob) {
            this.threadPoolJob = threadPoolJob;
        }

        @Override
        protected void runInternal() throws Exception {
            threadpool.setJob(threadPoolJob);
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
}
