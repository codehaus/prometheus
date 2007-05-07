package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.testsupport.TestUtil;

import java.util.concurrent.TimeUnit;

public class TaskProducer extends TestThread {
    private final int taskCount;
    private final BlockingExecutor executor;

    public TaskProducer(int taskCount, BlockingExecutor executor) {
        this.taskCount = taskCount;
        this.executor = executor;
    }

    @Override
    protected void runInternal() throws Exception {
        System.out.println("tASK PROCEUDER");
        for (int k = 0; k < taskCount; k++) {
            executor.execute(new Task());
            System.out.println("task produced");
            TestUtil.sleepRandom(20, TimeUnit.MILLISECONDS);
        }
    }
}

