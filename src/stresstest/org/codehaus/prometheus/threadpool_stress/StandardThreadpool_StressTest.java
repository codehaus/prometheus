package org.codehaus.prometheus.threadpool_stress;

import org.codehaus.prometheus.concurrenttesting.ConcurrentTestCase;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.pi;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.randomInt;
import org.codehaus.prometheus.threadpool.StandardThreadPool;
import org.codehaus.prometheus.threadpool.ThreadPoolJob;

import java.util.concurrent.atomic.AtomicInteger;

public class StandardThreadpool_StressTest extends ConcurrentTestCase {
    private StandardThreadPool threadpool;
    private static final int WORK_COUNT = 100000;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        threadpool = new StandardThreadPool();
    }

    @Override
    public void tearDown() throws Exception {
        super.setUp();
    }

    public void test_1() throws Exception {
        test(1);
    }

    public void test_5() throws Exception {
        test(5);
    }

    public void test_10() throws Exception {
        test(10);
    }

    public void test_30() throws Exception {
        test(30);
    }

    public void test_100() throws Exception {
        test(100);
    }

    public void test_300() throws Exception {
        test(300);
    }

    public void test(int numberOfParallelThreads) throws InterruptedException {
        for (int k = 0; k < numberOfParallelThreads; k++)
            threadpool.spawn(new Job(WORK_COUNT));

        threadpool.shutdownPolitly();
        threadpool.awaitShutdown();
    }

    class Job implements ThreadPoolJob {
        private final AtomicInteger count = new AtomicInteger();

        public Job(int workCount) {
            count.set(workCount);
        }

        public Object takeWork() {
            return "";
        }

        public boolean executeWork(Object work) {
            if (count.decrementAndGet() == 0) {
                return false;
            }

            pi(randomInt(100));
            return true;
        }
    }
}
