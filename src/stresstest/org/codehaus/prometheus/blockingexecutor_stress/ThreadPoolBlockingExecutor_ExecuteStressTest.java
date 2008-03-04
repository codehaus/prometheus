package org.codehaus.prometheus.blockingexecutor_stress;

import org.codehaus.prometheus.blockingexecutor.ThreadPoolBlockingExecutor;
import org.codehaus.prometheus.concurrenttesting.ConcurrentTestCase;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.joinAll;
import org.codehaus.prometheus.concurrenttesting.TestThread;
import org.codehaus.prometheus.exceptionhandler.TracingExceptionHandler;
import org.codehaus.prometheus.threadpool.StandardThreadPool;
import org.codehaus.prometheus.util.StandardThreadFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

/**
 * Stress tests the execution of tasks in the {@link org.codehaus.prometheus.blockingexecutor.ThreadPoolBlockingExecutor}. It isn't
 * means as a performance test.
 * <p/>
 * Improvements:
 * -test with different the number of thread in the threadpool
 */
public class ThreadPoolBlockingExecutor_ExecuteStressTest extends ConcurrentTestCase {
    private volatile ThreadPoolBlockingExecutor executor;
    private volatile List<ProduceThread> producerList;
    private volatile TracingExceptionHandler exceptionHandler;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        ThreadFactory factory = new StandardThreadFactory(Thread.MIN_PRIORITY, "test");
        executor = new ThreadPoolBlockingExecutor(
                10, new StandardThreadPool(factory), new LinkedBlockingQueue<Runnable>(50));
        executor.start();
        exceptionHandler = new TracingExceptionHandler();
        executor.setExceptionHandler(exceptionHandler);
        producerList = new LinkedList<ProduceThread>();
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

    public void test_1() throws Exception {
        stressTest(1, 10);
    }

    public void test_1_a() throws Exception {
        stressTest(1, 100);
    }

    public void test_1_b() throws Exception {
        stressTest(1, 300);
    }

    public void test_2_a() throws Exception {
        stressTest(10, 1);
    }

    public void test_2_b() throws Exception {
        stressTest(10, 10);
    }

    public void test_2_c() throws Exception {
        stressTest(10, 300);
    }

    public void test_3() throws Exception {
        stressTest(100, 1);
    }

    public void test_4() throws Exception {
        stressTest(100, 10);
    }

    public void test_5() throws Exception {
        stressTest(50, 50);
    }

    /**
     * @param producerCount              the number of producers
     * @param taskCountForSingleProducer the total number of times a procuder should repeat his producing task
     * @throws InterruptedException
     */
    public void stressTest(int producerCount, int taskCountForSingleProducer) throws InterruptedException {
        startProducers(producerCount, taskCountForSingleProducer);
        waitForAllWorkProcessed();
        assertOk();
    }

    private void assertOk() {
        for (ProduceThread producer : producerList)
            producer.assertSuccess();

        exceptionHandler.printStacktraces();
        exceptionHandler.assertNoErrors();
    }

    private void startProducers(int producerCount, int taskCount) {
        producerList = new LinkedList<ProduceThread>();
        for (int k = 0; k < producerCount; k++) {
            ProduceThread producer = scheduleProduce(taskCount);
            producerList.add(producer);
        }
    }

    public ProduceThread scheduleProduce(int count) {
        ProduceThread thread = new ProduceThread(count, executor);
        thread.start();
        return thread;
    }

    private void waitForAllWorkProcessed() throws InterruptedException {
        waitForProducersToComplete();
        executor.shutdownPolitly();
        executor.awaitShutdown();
        assertTrue("item remainings: " + executor.getWorkQueue().size(), executor.getWorkQueue().isEmpty());
    }

    public void waitForProducersToComplete() {
        for (ProduceThread producer : producerList) {
            try {
                //todo: using a join could lead to late detection of too long blocking producer (test won't finish)
                producer.join();
                producer.assertIsTerminatedNormally();
            } catch (InterruptedException e) {
                fail("unexpected InterruptedException");
            }
        }
    }
}
