package org.codehaus.prometheus.threadpool;

public class DummyThreadPoolJob implements ThreadPoolJob {

    public Object getWork() throws InterruptedException {
        throw new RuntimeException();
    }

    public Object getShuttingdownWork() throws InterruptedException {
        throw new RuntimeException();
    }

    public boolean executeWork(Object task) throws Exception {
        throw new RuntimeException();
    }
}
