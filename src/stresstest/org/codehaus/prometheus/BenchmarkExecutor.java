package org.codehaus.prometheus;

import java.util.concurrent.TimeUnit;

public class BenchmarkExecutor {

    private final long count;

    public BenchmarkExecutor(long count) {
        this.count = count;
    }

    public long runBenchmark(BenchmarkTask benchmarkTask) throws Exception {
        benchmarkonce(benchmarkTask);

        long totalNs = 0;
        for (int k = 0; k < count; k++) {
            totalNs += benchmarkonce(benchmarkTask);
        }
        return totalNs / count;
    }

    private long benchmarkonce(BenchmarkTask benchmarkTask) throws Exception {
        benchmarkTask.beforeRun();
        System.out.println("starting " + benchmarkTask);
        long startNs = System.nanoTime();
        benchmarkTask.run();
        long endNs = System.nanoTime();
        long timeNs = endNs - startNs;
        System.out.println(String.format("finished %s in %s ms", benchmarkTask, TimeUnit.NANOSECONDS.toMillis(timeNs)));
        return timeNs;
    }
}
