package org.codehaus.prometheus.threadpool;

/**
 * Unittests {@link StandardThreadPool#start()} method.
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool_StartTest extends StandardThreadPool_AbstractTest{

    public void testWhileUnstartedAndNoDefaultWorkJob(){
        newUnstartedThreadPoolWithoutDefaultJob(10);

        StartThread startThread = scheduleStart();
        joinAll(startThread);

        startThread.assertIsTerminatedWithThrowing(IllegalStateException.class);
        assertIsUnstarted();
        threadPoolThreadFactory.assertCreatedCount(0);
    }

    public void testWhileUnstarted(){
        int count = 100;
        newUnstartedThreadPool(count);

        StartThread startThread = scheduleStart();
        joinAll(startThread);

        startThread.assertIsTerminatedWithoutThrowing();
        assertIsStarted();
        threadPoolThreadFactory.assertCreatedCount(count);
    }

    public void testStarted(){
        int poolsize = 100;
        newStartedThreadpool(poolsize);

        StartThread startThread = scheduleStart();
        joinAll(startThread);

        startThread.assertIsTerminatedWithoutThrowing();
        assertIsStarted();
        threadPoolThreadFactory.assertCreatedCount(poolsize);
    }

    public void testWhileShuttingdown(){
        newShuttingdownThreadpool(10,DELAY_EON_MS);

        StartThread startThread = scheduleStart();
        joinAll(startThread);

        startThread.assertIsTerminatedWithThrowing(IllegalStateException.class);
        assertIsShuttingdown();

        //todo: test that no additional threads have been created
    }

    public void testWhileShutdown(){
        newShutdownThreadpool();
        int oldpoolsize = threadPoolThreadFactory.getThreadCount();

        StartThread startThread = scheduleStart();
        joinAll(startThread);

        startThread.assertIsTerminatedWithThrowing(IllegalStateException.class);
        assertIsShutdown();
        threadPoolThreadFactory.assertCreatedCount(oldpoolsize);
    }
}
