package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.exceptionhandler.TracingExceptionHandler;
import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.testsupport.TestThread;
import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.joinAll;
import org.codehaus.prometheus.threadpool.StandardThreadPool;
import org.codehaus.prometheus.util.StandardThreadFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

/**
 * Stress tests the execution of tasks in the {@link ThreadPoolBlockingExecutor}. It isn't
 * means as a performance test.
 * <p/>
 * Improvements:
 * -test with different the number of thread in the threadpool
 */
public class ThreadPoolBlockingExecutor_ExecuteStressTest extends ConcurrentTestCase {
    private volatile ThreadPoolBlockingExecutor executor;
    private volatile List<StressTaskProducer> workerList;
    private volatile TracingExceptionHandler exceptionHandler;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        ThreadFactory factory = new StandardThreadFactory(Thread.MIN_PRIORITY, "test");
        executor = new ThreadPoolBlockingExecutor(new StandardThreadPool(10, factory), new LinkedBlockingQueue<Runnable>(50));
        executor.start();
        exceptionHandler = new TracingExceptionHandler();
        executor.setExceptionHandler(exceptionHandler);
        workerList = new LinkedList<StressTaskProducer>();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        TestThread t = new TestThread() {
            protected void runInternal() throws Exception {
                executor.shutdownNow();
                executor.awaitShutdown();
            }
        };

        t.start();
        joinAll(t);
        t.assertIsTerminatedNormally();
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

    /**
     * @param producerCount the number of producers
     * @param repeatCount   the total number of times a procuder should repeat his producing task
     * @throws InterruptedException
     */
    public void stressTest(int producerCount, int repeatCount) throws InterruptedException {
        List<StressTaskProducer> producerList = startProducers(producerCount, repeatCount);
        waitForAllWorkProcessed();
        assertOk(producerList);
    }

    private void assertOk(List<StressTaskProducer> producerList) {
        for(StressTaskProducer producer: producerList)
            producer.assertSuccess();

        exceptionHandler.printStacktraces();
        exceptionHandler.assertNoErrors();
    }

    private List<StressTaskProducer> startProducers(int producerCount, int taskCount) {
        List<StressTaskProducer> producerList = new LinkedList<StressTaskProducer>();
        for (int k = 0; k < producerCount; k++){
            StressTaskProducer producer = scheduleWorker(taskCount);
            producerList.add(producer);
        }

        return producerList;
    }

    private void waitForAllWorkProcessed() throws InterruptedException {
        waitForWorkersToComplete();
        executor.shutdown();
        executor.awaitShutdown();
        assertTrue(executor.getWorkQueue().isEmpty());
    }

    public void waitForWorkersToComplete() {
        for (StressTaskProducer t : workerList) {
            try {
                //todo: using a join could lead to undetected concurrency problems, so fix it
                t.join();
                t.assertIsTerminatedNormally();
            } catch (InterruptedException e) {
                fail("unexpected InterruptedException");
            }
        }
    }

    public StressTaskProducer scheduleWorker(int count) {
        StressTaskProducer thread = new StressTaskProducer(count, executor);
        workerList.add(thread);
        thread.start();
        return thread;
    }
}
