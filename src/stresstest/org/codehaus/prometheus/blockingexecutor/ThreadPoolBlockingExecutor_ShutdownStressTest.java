package org.codehaus.prometheus.blockingexecutor;        

import junit.framework.TestSuite;
import org.codehaus.prometheus.exceptionhandler.TracingExceptionHandler;
import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.testsupport.TestUtil;
import org.codehaus.prometheus.threadpool.StandardThreadPool;
import org.codehaus.prometheus.util.StandardThreadFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stress tests {@link ThreadPoolBlockingExecutor#shutdown()} method.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor_ShutdownStressTest extends ConcurrentTestCase {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        for (int k = 0; k < 10; k++) {
            suite.addTest(new ShutdownTest(1, 1, 10, 10));
            suite.addTest(new ShutdownTest(10, 10, 10, 10));
            suite.addTest(new ShutdownTest(10, 1000, 50, 50));
            suite.addTest(new ShutdownTest(1000, 10, 10, 10));
            suite.addTest(new ShutdownTest(10, 10, 1000, 10));
            suite.addTest(new ShutdownTest(10, 10, 10, 1000));
        }
        return suite;
    }

    public static class ShutdownTest extends ConcurrentTestCase {
        private volatile ThreadPoolBlockingExecutor executor;
        private volatile AtomicInteger executedCount;
        private volatile AtomicInteger placedCount;
        private volatile List<TaskProducer> workerList;
        private volatile TracingExceptionHandler exceptionHandler;

        private final int poolsize;
        private final int queuesize;
        private final int producercount;
        private final int workcount;

        public ShutdownTest(int poolsize, int queuesize, int producercount, int workcount) {
            super(String.format("poolsize=%d queuesize=%d producercount=%d workcount=%d", poolsize, queuesize, producercount, workcount));
            this.poolsize = poolsize;
            this.queuesize = queuesize;
            this.producercount = producercount;
            this.workcount = workcount;
        }

        @Override
        public void setUp() throws Exception {
            super.setUp();

            ThreadFactory factory = new StandardThreadFactory(Thread.MIN_PRIORITY, "test");
            executor = new ThreadPoolBlockingExecutor(
                    new StandardThreadPool(poolsize, factory),
                    new LinkedBlockingQueue<Runnable>(queuesize));
            executor.start();
            exceptionHandler = new TracingExceptionHandler();
            executor.setExceptionHandler(exceptionHandler);
            executedCount = new AtomicInteger();
            placedCount = new AtomicInteger();
            workerList = new LinkedList<TaskProducer>();
        }

        public void tearDown() throws Exception {
            super.tearDown();
            executor.shutdownNow();
        }

        public void runTest() throws InterruptedException {
            startProducers();
            shutdownThreadpoolexecutor();
            assertOk();
        }

        private void assertOk() {
            assertEquals(placedCount.intValue(), executedCount.intValue());
            exceptionHandler.printStacktraces();
            exceptionHandler.assertNoErrors();
        }

        private void startProducers() {
            for (int k = 0; k < producercount; k++)
                scheduleProducer();
        }

        private void shutdownThreadpoolexecutor() throws InterruptedException {
            TestUtil.sleepRandomMs(1000);
            executor.shutdown();
            executor.awaitShutdown();

            sleepMs(DELAY_LONG_MS);
            assertTrue(executor.getWorkQueue().isEmpty());
            assertAllProducersFinished();
        }

        public void assertAllProducersFinished() {
            for (TaskProducer t : workerList)
                t.assertIsTerminatedNormally();
        }

        public TaskProducer scheduleProducer() {
            TaskProducer thread = new TaskProducer();
            workerList.add(thread);
            thread.start();
            return thread;
        }

        class TaskProducer extends TestThread {
            @Override
            protected void runInternal() throws Exception {
                try {
                    for (int k = 0; k < workcount; k++) {
                        TestUtil.sleepRandom(20, TimeUnit.MILLISECONDS);
                        executor.execute(new Task());
                        placedCount.incrementAndGet();
                    }
                } catch (RejectedExecutionException ex) {
                    //ignore it
                }
            }
        }

        public class Task implements Runnable {
            public void run() {
                executedCount.incrementAndGet();
                TestUtil.sleepRandom(20, TimeUnit.MILLISECONDS);
                TestUtil.someCalculation(100000);
            }
        }
    }
}
