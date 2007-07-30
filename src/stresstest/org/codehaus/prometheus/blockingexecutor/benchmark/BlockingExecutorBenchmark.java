package org.codehaus.prometheus.blockingexecutor.benchmark;

import org.codehaus.prometheus.blockingexecutor.BlockingExecutorService;
import org.codehaus.prometheus.blockingexecutor.ThreadPoolBlockingExecutor;
import org.codehaus.prometheus.util.StandardThreadFactory;
import org.codehaus.prometheus.BenchmarkTask;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class BlockingExecutorBenchmark implements BenchmarkTask {
    private final List<Runnable> tasklist;
    private final int threadcount;
    private final BlockingQueue<Runnable> workQueue;
    private BlockingExecutorService executor;


    public BlockingExecutorBenchmark(int threadcount, List<Runnable> tasklist, BlockingQueue<Runnable> workQueue) {
        this.threadcount = threadcount;
        this.tasklist = tasklist;
        this.workQueue = workQueue;
    }

    public void beforeRun() throws Exception {
        executor = new ThreadPoolBlockingExecutor(threadcount,
                new StandardThreadFactory(),
                workQueue);
        executor.start();
    }

    public void run() throws InterruptedException {
        for (Runnable task : tasklist)
            executor.execute(task);

        executor.shutdown();
        executor.awaitShutdown();
    }

    public String toString() {
        return "BlockingExecutorBenchmark";
    }
}