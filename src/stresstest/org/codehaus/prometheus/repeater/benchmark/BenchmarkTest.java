package org.codehaus.prometheus.repeater.benchmark;

import junit.framework.TestCase;
import org.codehaus.prometheus.BenchmarkExecutor;
import org.codehaus.prometheus.BenchmarkTask;
import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.randomInt;

import java.util.concurrent.TimeUnit;

public class BenchmarkTest extends TestCase {

    public void test() throws Exception {
        int poolsize = 1;
        System.out.println("generating load");
        int[] tasks = createLoad(100000);
        System.out.println("finished generating load");

        BenchmarkExecutor executor = new BenchmarkExecutor(5);

        BenchmarkTask tprbenchmark = new ThreadPoolRepeaterBenchmark(poolsize,tasks);
        BenchmarkTask plainTask = new PlainThreadBenchmark(tasks,poolsize);

        long tprNs = executor.runBenchmark(tprbenchmark);
        long plainNs = executor.runBenchmark(plainTask);
        System.out.println(String.format("threadpoolrepeater %s ms",TimeUnit.NANOSECONDS.toMillis(tprNs)));
        System.out.println(String.format("plainthreads %s ms",TimeUnit.NANOSECONDS.toMillis(plainNs)));
    }

    public int[] createLoad(int lenght){
        int[] load = new int[lenght];
        for(int k=0;k<lenght;k++)
            load[k]=randomInt(10000);
        return load;
    }
}
