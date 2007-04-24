package org.codehaus.prometheus.threadpool;

public class DummyWorkerJob implements WorkerJob{

    public Object getWork() throws InterruptedException {
        throw new RuntimeException();
    }

    public Object getWorkWhileShuttingdown() throws InterruptedException {
        throw new RuntimeException();
    }

    public void runWork(Object task) throws Exception {
        throw new RuntimeException();
    }
}
