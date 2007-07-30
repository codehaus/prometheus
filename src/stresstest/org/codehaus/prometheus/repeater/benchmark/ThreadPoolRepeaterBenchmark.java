package org.codehaus.prometheus.repeater.benchmark;

import org.codehaus.prometheus.BenchmarkTask;
import org.codehaus.prometheus.repeater.EndRepeaterStrategy;
import org.codehaus.prometheus.repeater.ThreadPoolRepeater;

public class ThreadPoolRepeaterBenchmark implements BenchmarkTask {
    private final int[] load;
    private final int threadcount;
    private ThreadPoolRepeater repeater;

    public ThreadPoolRepeaterBenchmark(int threadcount, int[] load) {
        this.threadcount = threadcount;
        this.load = load;
    }

    public void beforeRun() throws Exception {
        repeater = new ThreadPoolRepeater(new StressRepeatable(load),threadcount);
        repeater.setRepeatableExecutionStrategy(new EndRepeaterStrategy());
    }

    public void run() throws Exception {
        repeater.start();
        repeater.awaitShutdown();
    }

    public String toString(){
        return "ThreadPoolRepeater";
    }
}
