package org.codehaus.prometheus.blockingexecutor;

import static junit.framework.Assert.assertEquals;
import org.codehaus.prometheus.testsupport.TestThread;
import static org.codehaus.prometheus.testsupport.TestUtil.sleepRandomMs;

import java.util.concurrent.atomic.AtomicLong;

public class StressTaskProducer extends TestThread {
    private final long taskCount;
    private final BlockingExecutor executor;
    private final AtomicLong executionCount = new AtomicLong();

    public StressTaskProducer(long taskCount, BlockingExecutor executor) {
        this.taskCount = taskCount;
        this.executor = executor;
    }

    public void assertSuccess(){
        assertIsTerminatedNormally();
        assertEquals(taskCount,executionCount.longValue());
    }

    @Override
    protected void runInternal() throws Exception {
        for (int k = 0; k < taskCount; k++) {
            executor.execute(new StressTask(executionCount));
            sleepRandomMs(20);
        }
    }
}

