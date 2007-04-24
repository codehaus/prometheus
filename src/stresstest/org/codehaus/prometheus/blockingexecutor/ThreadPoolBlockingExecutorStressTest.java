package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.testsupport.TestUtil;
import org.codehaus.prometheus.exceptionhandler.TracingExceptionHandler;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.LinkedList;

/**
 * Improvements:
 * -make the queue bounded
 * -test with different the number of thread in the threadpool
 */
public class ThreadPoolBlockingExecutorStressTest extends ConcurrentTestCase {
    private volatile ThreadPoolBlockingExecutor executor;
    private volatile AtomicInteger executedCount;
    private volatile List<TaskProducer> workerList;
    private volatile TracingExceptionHandler exceptionHandler;

    public void setUp() {
        executor = new ThreadPoolBlockingExecutor(10);
        executor.start();
        exceptionHandler = new TracingExceptionHandler();
        executor.setExceptionHandler(exceptionHandler);
        executedCount = new AtomicInteger();
        workerList = new LinkedList<TaskProducer>();
    }

    public void tearDown() {
        executor.shutdownNow();
    }

    public void test_1() throws InterruptedException {
        stressTest(1, 10);
    }

    public void test_2() throws InterruptedException {
        stressTest(11, 100);
    }

    public void test_3() throws InterruptedException {
        stressTest(100, 1);
    }

    public void test_4() throws InterruptedException {
        stressTest(100, 10);
    }

    //public void test_5() throws InterruptedException {
    //    stressTest(100, 1000);
    //}

    public void stressTest(int producerCount, int repeatCount) throws InterruptedException {
        placeWork(producerCount, repeatCount);
        waitForAllWorkProcessed();
        assertOk(producerCount, repeatCount);
    }

    private void assertOk(int producerCount, int repeatCount) {
        int expectedCount = producerCount * repeatCount;
        assertEquals(expectedCount, executedCount.intValue());
        exceptionHandler.assertNoErrors();
    }

    private void placeWork(int producerCount, int repeatCount) {
        for (int k = 0; k < producerCount; k++)
            scheduleWorker(repeatCount);
    }

    private void waitForAllWorkProcessed() throws InterruptedException {
        waitForCompletionWorkers();
        executor.shutdown();
        executor.awaitShutdown();
        assertTrue(executor.getWorkQueue().isEmpty());
    }

    public void waitForCompletionWorkers() {
        for (TaskProducer t : workerList) {
            try {
                t.join();
                t.assertIsTerminatedWithoutThrowing();
            } catch (InterruptedException e) {
                fail("unexpected InterruptedException");
            }
        }
    }

    public TaskProducer scheduleWorker(int count) {
        TaskProducer thread = new TaskProducer(count);
        workerList.add(thread);
        thread.start();
        return thread;
    }

    class TaskProducer extends TestThread {
        private final int taskCount;

        public TaskProducer(int taskCount) {
            this.taskCount = taskCount;
        }

        protected void runInternal() throws Exception {
            for (int k = 0; k < taskCount; k++) {
                executor.execute(new Task());
                TestUtil.sleepRandom(20, TimeUnit.MILLISECONDS);
            }
        }
    }

    public class Task implements Runnable {
        public void run() {
            executedCount.incrementAndGet();
            TestUtil.sleepRandom(20, TimeUnit.MILLISECONDS);
        }
    }
}
