package org.codehaus.prometheus.blockingexecutor_stress.benchmark;

import org.codehaus.prometheus.BenchmarkExecutor;
import org.codehaus.prometheus.concurrenttesting.ConcurrentTestCase;
import static org.codehaus.prometheus.concurrenttesting.TestSupport.newStressRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BlockingExecutorBenchmarkTest extends ConcurrentTestCase {

    public void test() throws Exception {
        int poolsize = 50;
        int tasklistsize = 100000;
        int repeatCount = 10;

        BenchmarkExecutor executor = new BenchmarkExecutor(repeatCount);

        System.out.println("generating tasklist");
        List<Runnable> tasklist = generateTaskList(tasklistsize);
        System.out.println("tasklist generated: " + tasklist.size());

        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(10000);
        long blockingNs = executor.runBenchmark(
                new BlockingExecutorBenchmark(poolsize, tasklist,workQueue));
        long executorNs = executor.runBenchmark(
                new ThreadPoolExecutorBenchmark(poolsize, tasklist,workQueue));
        long plainQueueNs = executor.runBenchmark(
                new PlainThreadBlockingQueueBenchmark(poolsize, tasklist,workQueue));
        long plainNs = executor.runBenchmark((new PlainThreadBenchmark(poolsize, tasklist)));

        System.out.println("normalMs " + TimeUnit.NANOSECONDS.toMillis(executorNs));
        System.out.println("blockingMs " + TimeUnit.NANOSECONDS.toMillis(blockingNs));
        System.out.println("plainMs " + TimeUnit.NANOSECONDS.toMillis(plainNs));
        System.out.println("plainQueueMs " + TimeUnit.NANOSECONDS.toMillis(plainQueueNs));
    }


    public List<Runnable> generateTaskList(int count) {
        List<Runnable> list = new ArrayList<Runnable>(count);
        for (int k = 0; k < count; k++) {
            Runnable task = newStressRunnable();
            list.add(task);
        }

        return list;
    }
}
