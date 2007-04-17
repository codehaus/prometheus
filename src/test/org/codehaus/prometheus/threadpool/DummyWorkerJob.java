package org.codehaus.prometheus.threadpool;

public class DummyWorkerJob implements WorkerJob{

    public Object getTask() throws InterruptedException {
        throw new RuntimeException();
    }

    public boolean executeTask(Object task) throws Exception {
        throw new RuntimeException();
    }
}
