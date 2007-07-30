package org.codehaus.prometheus.blockingexecutor.benchmark;

import org.codehaus.prometheus.BenchmarkTask;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorBenchmark implements BenchmarkTask {
    private final List<Runnable> tasklist;
    private final int poolsize;
    private final BlockingQueue<Runnable> workQueue;
    private ThreadPoolExecutor executor;

    public ThreadPoolExecutorBenchmark(int poolsize, List<Runnable> tasklist, BlockingQueue<Runnable> workQueue) {
        this.tasklist = tasklist;
        this.poolsize = poolsize;
        this.workQueue = workQueue;
    }

    public void beforeRun() throws Exception {
        workQueue.clear();

        executor = new ThreadPoolExecutor(
                poolsize, poolsize, 0, TimeUnit.NANOSECONDS,
                workQueue);

        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
                try {
                    executor.getQueue().put(task);
                } catch (InterruptedException e) {
                    throw new RuntimeException("lets fail the benchmark");
                }
            }
        });
    }

    public void run() throws InterruptedException {
        for (Runnable task : tasklist)
            executor.execute(task);

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    public String toString() {
        return "ExecutorBenchmark";
    }
}
