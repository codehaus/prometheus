package org.codehaus.prometheus.threadpool;

/**
 * Unittests the {@link StandardThreadPool#shutdownNow()} method.
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool_ShutdownNowTest extends StandardThreadPool_AbstractTest {

    public void testWhileUnstarted(){
        newUnstartedThreadPool(10);

        ShutdownNowThread shutdownThread = scheduleShutdownNow();
        joinAll(shutdownThread);

        sleepMs(DELAY_SMALL_MS);
        shutdownThread.assertIsTerminatedWithoutThrowing();
        assertIsShutdown();        
    }

    public void testWhileRunningAndEmptyPool(){
        newStartedThreadpool(0);

        ShutdownNowThread shutdownNowThread = scheduleShutdownNow();
        joinAll(shutdownNowThread);

        shutdownNowThread.assertIsTerminatedWithoutThrowing();
        sleepMs(DELAY_SMALL_MS);
        assertIsShutdown();
    }

    public void testIdleWorkersAreInterrupted(){
        newStartedThreadpool(10);

        ShutdownNowThread shutdownNowThread = scheduleShutdownNow();
        joinAll(shutdownNowThread);

        shutdownNowThread.assertIsTerminatedWithoutThrowing();
        sleepMs(DELAY_SMALL_MS);
        assertIsShutdown();
    }

    public void testNonIdleWorkersAreInterrupted() throws InterruptedException {
        int poolsize = 10;
        newStartedThreadpool(poolsize);
        letWorkersWork(poolsize,DELAY_EON_MS);

        ShutdownNowThread shutdownNowThread = scheduleShutdownNow();
        joinAll(shutdownNowThread);

        shutdownNowThread.assertIsTerminatedWithoutThrowing();
        sleepMs(DELAY_SMALL_MS);
        assertIsShutdown();
    }


    public void testWhileShuttingDown(){
        newShuttingdownThreadpool(10,DELAY_EON_MS);

        ShutdownNowThread shutdownNowThread = scheduleShutdownNow();
        joinAll(shutdownNowThread);

        shutdownNowThread.assertIsTerminatedWithoutThrowing();
        sleepMs(DELAY_SMALL_MS);
        assertIsShutdown();
    }

    public void testWhileShutdown(){
        newShutdownThreadpool();

        ShutdownNowThread shutdownThread = scheduleShutdownNow();
        joinAll(shutdownThread);

        assertIsShutdown();
        shutdownThread.assertIsTerminated();
    }
}
