package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.testsupport.SleepingRunnable;
import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.testsupport.TracingThreadFactory;
import org.codehaus.prometheus.util.StandardThreadFactory;
import org.codehaus.prometheus.exceptionhandler.TracingExceptionHandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class StandardThreadPool_AbstractTest extends ConcurrentTestCase {

    protected volatile StandardThreadPool threadpool;
    protected volatile TracingThreadFactory threadPoolThreadFactory;
    protected volatile BlockingQueue<Runnable> taskQueue;
    protected volatile TracingExceptionHandler threadPoolExceptionHandler;

    public class TestWorkerJob implements WorkerJob<Runnable> {

        public Runnable getTask() throws InterruptedException {
            return taskQueue.take();
        }

        public boolean executeTask(Runnable task) throws Exception {
            task.run();
            return true;
        }
    }

    /**
     * Places the given number of job on the taskqueue. Each job is a SleepingRunnable
     * (can be interrupted) that works for an eon.
     *
     * @param jobcount
     * @throws InterruptedException
     */
    public void letWorkersWork(int jobcount, long delayMs) {
        if (jobcount < 0) throw new IllegalArgumentException();

        try {
            for (int k = 0; k < jobcount; k++)
                taskQueue.put(new SleepingRunnable(delayMs));
        } catch (InterruptedException ex) {
            fail("unexpected interrupted exception");
        }
    }

    public void setUp() throws Exception {
        super.setUp();

        taskQueue = new LinkedBlockingQueue<Runnable>();
    }

    public void newStartedThreadpool() {
        newUnstartedThreadPool();
        threadpool.start();
    }

    public void newStartedThreadpool(int poolsize) {
        newUnstartedThreadPool(poolsize);
        threadpool.start();
    }

    public void newShuttingdownThreadpool(int poolsize, long runningTimeMs) {
        newStartedThreadpool(poolsize);

        letWorkersWork(poolsize,runningTimeMs);
        //give workers time to start executing the task.
        sleepMs(DELAY_SMALL_MS);
        //make sure that all workers are executing a job.
        assertTrue(taskQueue.isEmpty());

        //shut down the threadpool
        ShutdownThread shutdownThread = scheduleShutdown();
        joinAll(shutdownThread);
        shutdownThread.assertIsTerminatedWithoutThrowing();
        assertIsShuttingdown();
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
        threadpool.setWorkerJob(new TestWorkerJob());
    }

    public void newShutdownThreadpool() {
        newStartedThreadpool();
        threadpool.shutdown();
    }

    public void assertIsStarted() {
        assertEquals(ThreadPoolState.started, threadpool.getState());
    }

    public void assertIsUnstarted() {
        assertEquals(ThreadPoolState.unstarted, threadpool.getState());
    }

    public void assertIsShutdown() {
        assertEquals(ThreadPoolState.shutdown, threadpool.getState());
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

    public void tearDown() throws Exception {
        super.tearDown();

        if (threadpool == null)
            return;

        TestThread t = new TestThread() {
            protected void runInternal() throws Exception {
                threadpool.shutdownNow();
                threadpool.awaitShutdown();
            }
        };
        t.start();
        joinAll(t);
        t.assertIsTerminatedWithoutThrowing();
        assertIsShutdown();
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

    public SetDefaultWorkerJobThread scheduleSetDefaultWorkerJob(WorkerJob workerJob) {
        SetDefaultWorkerJobThread t = new SetDefaultWorkerJobThread(workerJob);
        t.start();
        return t;
    }

    public class SetDesiredPoolsizeThread extends TestThread {
        private final int poolsize;

        public SetDesiredPoolsizeThread(int poolsize) {
            this.poolsize = poolsize;
        }

        protected void runInternal() throws InterruptedException, TimeoutException {
            threadpool.setDesiredPoolsize(poolsize);
        }
    }

    public class StartThread extends TestThread {
        protected void runInternal() {
            threadpool.start();
        }
    }

    public class ShutdownNowThread extends TestThread {
        protected void runInternal() throws InterruptedException, TimeoutException {
            threadpool.shutdownNow();
        }
    }

    public class ShutdownThread extends TestThread {
        protected void runInternal() {
            threadpool.shutdown();
        }
    }

    public class SetDefaultWorkerJobThread extends TestThread {
        private final WorkerJob workerJob;

        public SetDefaultWorkerJobThread(WorkerJob workerJob) {
            this.workerJob = workerJob;
        }

        protected void runInternal() throws Exception {
            threadpool.setWorkerJob(workerJob);
        }
    }

    public class TryAwaitShutdownThread extends TestThread {
        final long timeoutMs;

        public TryAwaitShutdownThread(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        public void runInternal() throws TimeoutException, InterruptedException {
            threadpool.tryAwaitShutdown(timeoutMs, TimeUnit.MILLISECONDS);
        }
    }

    public class AwaitShutdownThread extends TestThread {
        protected void runInternal() throws InterruptedException {
            threadpool.awaitShutdown();
        }
    }
}
