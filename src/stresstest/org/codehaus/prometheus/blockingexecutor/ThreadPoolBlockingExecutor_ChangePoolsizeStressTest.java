package org.codehaus.prometheus.blockingexecutor;

import junit.framework.TestSuite;
import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.testsupport.TestUtil;
import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.exceptionhandler.TracingExceptionHandler;

import java.util.List;
import java.util.LinkedList;

/**
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor_ChangePoolsizeStressTest {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new ChangeTest(1, 1, 1, 1, 1));
        return suite;
    }

    public static class ChangeTest extends ConcurrentTestCase {
        private final int nrchanges;
        private final int maxpoolsize;
        private final int concurrentsetters;
        private volatile ThreadPoolBlockingExecutor executor;
        private volatile TracingExceptionHandler exceptionHandler;
        private final List<TaskProducer> workerList = new LinkedList<TaskProducer>();
        private final int workercount;
        private final int taskcount;

        public ChangeTest(int concurrentsetters, int nrchanges, int maxpoolsize, int workercount, int taskcount) {
            this.nrchanges = nrchanges;
            this.maxpoolsize = maxpoolsize;
            this.concurrentsetters = concurrentsetters;
            this.workercount = workercount;
            this.taskcount = taskcount;
        }

        @Override
        public void setUp() throws Exception {
            super.setUp();

            exceptionHandler = new TracingExceptionHandler();

            executor = new ThreadPoolBlockingExecutor(0);
            executor.setExceptionHandler(exceptionHandler);
            executor.start();
        }

        @Override
        public void tearDown() throws Exception {
            super.tearDown();
        }

        @Override
        public void runTest() {
            scheduleTaskProducers(workercount,taskcount);
            startSizeChangeThreads();

            TestUtil.sleepMs(10000);
        }

        public void scheduleTaskProducers(int workerCount, int taskCount) {
            for(int k=0;k<workerCount;k++)
                scheduleWorker(taskCount);
        }

        public TaskProducer scheduleWorker(int taskCount) {
            TaskProducer thread = new TaskProducer(taskCount, executor);
            workerList.add(thread);
            thread.start();
            return thread;
        }


        private void startSizeChangeThreads() {
            for (int k = 0; k < concurrentsetters; k++) {
                SizeThread t = new SizeThread(nrchanges, maxpoolsize, executor);
                t.start();
            }
        }
    }


    public static class SizeThread extends TestThread {
        private final int nrchanges;
        private final int maxpoolsize;
        private ThreadPoolBlockingExecutor executor;

        public SizeThread(int nrchanges, int maxpoolsize, ThreadPoolBlockingExecutor executor) {
            this.nrchanges = nrchanges;
            this.maxpoolsize = maxpoolsize;
            this.executor = executor;
        }

        @Override
        public void runInternal() {
            for (int k = 0; k < nrchanges; k++) {

                int poolsize = TestUtil.randomInt(maxpoolsize);
                executor.setDesiredPoolSize(poolsize);
                TestUtil.sleepMs(100);
            }
        }
    }
}
