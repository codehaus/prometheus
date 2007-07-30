package org.codehaus.prometheus.repeater.benchmark;

import org.codehaus.prometheus.BenchmarkTask;
import org.codehaus.prometheus.util.JucLatch;
import org.codehaus.prometheus.util.Latch;

import java.util.LinkedList;
import java.util.List;

public class PlainThreadBenchmark implements BenchmarkTask {

    private final int threadcount;
    private final int[] load;
    private final List<Thread> threadList = new LinkedList<Thread>();
    private Latch startLatch;
    private StressRepeatable task;

    public PlainThreadBenchmark(int[] load, int threadcount) {
        this.load = load;
        this.threadcount = threadcount;
    }

    public void beforeRun() throws Exception {
        startLatch = new JucLatch();
        threadList.clear();
        task = new StressRepeatable(load);

        for (int k = 0; k < threadcount; k++) {
            Thread t = new Thread(new Worker());
            t.start();
            threadList.add(t);
        }
    }

    public void run() throws Exception {
        startLatch.open();

        for (Thread thread : threadList)
            thread.join();
    }

    @Override
    public String toString(){
        return "Repeating with plain thread";
    }

    public class Worker implements Runnable {
        public void run() {
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }

            try {
                while(task.execute());
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }
    }
}
