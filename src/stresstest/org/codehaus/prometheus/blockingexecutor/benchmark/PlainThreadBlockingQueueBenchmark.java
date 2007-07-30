package org.codehaus.prometheus.blockingexecutor.benchmark;

import org.codehaus.prometheus.BenchmarkTask;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * This Benchmark uses a blockingqueue to store tasks. It uses a set of normal
 * threads to execute these tasks.
 *
 * @author Peter Veentjer
 */
public class PlainThreadBlockingQueueBenchmark implements BenchmarkTask {

    private final List<Thread> threadList = new LinkedList<Thread>();
    private final List<Runnable> tasklist;
    private final int threadcount;
    private final BlockingQueue<Runnable> workQueue;

    public PlainThreadBlockingQueueBenchmark(int threadcount, List<Runnable> tasklist, BlockingQueue<Runnable> workQueue) {
        this.threadcount = threadcount;
        this.tasklist = tasklist;
        this.workQueue = workQueue;
    }

    public void beforeRun() throws Exception {
        workQueue.clear();
        threadList.clear();

        for (int k = 0; k < threadcount; k++) {
            Thread thread = new Thread(new Worker());
            threadList.add(thread);
        }

        for (Thread thread : threadList)
            thread.start();
    }

    public void run() throws Exception {
        for (Runnable task : tasklist)
            workQueue.put(task);

        for (int k = 0; k < threadcount; k++)
            workQueue.put(new EndingTask());

        for (Thread thread : threadList)
            thread.join();
    }

    public String toString() {
        return "PlainThreadBlockingQueueBenchmark";
    }

    class EndingTask implements Runnable {
        public void run() {
            throw new EndOfWorkException();
        }
    }

    class EndOfWorkException extends RuntimeException {
    }

    class Worker implements Runnable {
        public void run() {
            try {
                while (true) {
                    Runnable task = null;
                    try {
                        task = workQueue.take();
                    } catch (InterruptedException e) {
                        throw new RuntimeException("fail this benchmark");
                    }
                    task.run();
                }
            } catch (EndOfWorkException ex) {

            }
        }
    }
}
