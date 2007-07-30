package org.codehaus.prometheus.blockingexecutor.benchmark;

import org.codehaus.prometheus.util.JucLatch;
import org.codehaus.prometheus.util.Latch;
import org.codehaus.prometheus.BenchmarkTask;

import java.util.LinkedList;
import java.util.List;

/**
 * Uses a set of normal threads and doesn't use a queue. An index of the list is shared
 * between threads, and a threads picks a task from this list. This is almost as low
 * on overhead as you can get.
 *
 * @author Peter Veentjer.
 */
public class PlainThreadBenchmark implements BenchmarkTask {

    private final List<Runnable> taskList;
    private final int threadcount;
    private final List<Thread> threadList = new LinkedList<Thread>();
    private int lastindex;
    private Latch latch;

    public PlainThreadBenchmark(int threadcount, List<Runnable> taskList) {
        this.taskList = taskList;
        this.threadcount = threadcount;
    }

    public void beforeRun() throws Exception {
        latch = new JucLatch();
        lastindex = 0;
        threadList.clear();

        for (int k = 0; k < threadcount; k++) {
            Thread thread = new Thread(new Worker());
            threadList.add(thread);
        }

        for (Thread thread : threadList)
            thread.start();
    }

    public void run() throws Exception {
        latch.open();
        
        for (Thread thread : threadList)
            thread.join();
    }

    private synchronized int nextIndex() {
        if (lastindex >= taskList.size()) {
            return -1;
        } else {
            int index = lastindex;
            lastindex++;
            return index;
        }
    }

    public String toString() {
        return "PlainThreadBenchmark";
    }

    class Worker implements Runnable {
        public void run() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException("lets fail the benchmark");
            }

            int index;
            while ((index = nextIndex()) > -1) {
                Runnable task = taskList.get(index);
                task.run();
            }
        }
    }
}

