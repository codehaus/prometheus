package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.exceptionhandler.TracingExceptionHandler;
import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.threadpool.StandardThreadPool;
import org.codehaus.prometheus.util.StandardThreadFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stress tests the execution of tasks in the {@link ThreadPoolBlockingExecutor}. It isn't
 * means as a performance test.
 *
 * Improvements:
 * -test with different the number of thread in the threadpool
 */
public class ThreadPoolBlockingExecutor_ExecuteStressTest extends ConcurrentTestCase {
    private volatile ThreadPoolBlockingExecutor executor;
    private volatile AtomicInteger executedCount;
    private volatile List<TaskProducer> workerList;
    private volatile TracingExceptionHandler exceptionHandler;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        ThreadFactory factory = new StandardThreadFactory(Thread.MIN_PRIORITY, "test");
        executor = new ThreadPoolBlockingExecutor(new StandardThreadPool(10, factory), new LinkedBlockingQueue<Runnable>(50));
        executor.start();
        exceptionHandler = new TracingExceptionHandler();
        executor.setExceptionHandler(exceptionHandler);
        executedCount = new AtomicInteger();
        workerList = new LinkedList<TaskProducer>();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        
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

    public void test_5() throws InterruptedException {
        stressTest(50, 50);
    }

    public void stressTest(int producerCount, int repeatCount) throws InterruptedException {
        placeWork(producerCount, repeatCount);
        waitForAllWorkProcessed();
        assertOk(producerCount, repeatCount);
    }

    private void assertOk(int producerCount, int repeatCount) {
        int expectedCount = producerCount * repeatCount;
        assertEquals(expectedCount, executedCount.intValue());
        exceptionHandler.printStacktraces();
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
        TaskProducer thread = new TaskProducer(count,executor);
        workerList.add(thread);
        thread.start();
        return thread;
    }


}
