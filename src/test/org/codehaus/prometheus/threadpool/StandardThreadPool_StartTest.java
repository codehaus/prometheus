package org.codehaus.prometheus.threadpool;

/**
 * Unittests {@link StandardThreadPool#start()} method.
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool_StartTest extends StandardThreadPool_AbstractTest {

    public void testWhileUnstartedAndNoDefaultWorkJob() {
        newUnstartedThreadPoolWithoutDefaultJob(10);

        StartThread startThread = scheduleStart();

        joinAll(startThread);
        startThread.assertIsTerminatedWithThrowing(IllegalStateException.class);
        assertIsUnstarted();
        threadPoolThreadFactory.assertCreatedCount(0);
    }

    public void testWhileUnstarted() {
        int count = 100;
        newUnstartedThreadPool(count);

        start();

        assertIsStarted();
        threadPoolThreadFactory.assertCreatedCount(count);
    }

    public void testStarted() {
        int poolsize = 100;
        newStartedThreadpool(poolsize);

        start();

        assertIsStarted();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
    }

    public void testWhileShuttingdown() {
        int poolsize = 10;
        newShuttingdownThreadpool(poolsize, DELAY_EON_MS);

        StartThread startThread = scheduleStart();

        joinAll(startThread);
        startThread.assertIsTerminatedWithThrowing(IllegalStateException.class);
        assertIsShuttingdown();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
    }

    public void testWhileShutdown() {
        newShutdownThreadpool();
        int oldpoolsize = threadPoolThreadFactory.getThreadCount();

        StartThread startThread = scheduleStart();

        joinAll(startThread);
        startThread.assertIsTerminatedWithThrowing(IllegalStateException.class);
        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(oldpoolsize);
    }


    private void start() {
        StartThread startThread = scheduleStart();

        joinAll(startThread);
        startThread.assertIsTerminatedNormally();
    }
}
