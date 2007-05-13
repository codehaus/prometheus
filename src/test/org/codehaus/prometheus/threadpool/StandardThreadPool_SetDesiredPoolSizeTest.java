package org.codehaus.prometheus.threadpool;

/**
 * Unittests the {@link StandardThreadPool#setDesiredPoolsize(int)} method.
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool_SetDesiredPoolSizeTest extends StandardThreadPool_AbstractTest {

    public void testNegativeValue() {
        newStartedThreadpool();
        int oldSize = threadpool.getDesiredPoolSize();

        try {
            threadpool.setDesiredPoolsize(-1);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        assertDesiredPoolsize(oldSize);
    }

    public void testNoChange() {
        int poolsize = 10;
        newStartedThreadpool(poolsize);

        setDesiredPoolSize(poolsize);

        assertIsStarted();
        assertActualPoolsize(poolsize);
        assertDesiredPoolsize(poolsize);
        threadPoolThreadFactory.assertCreatedCount(poolsize);
    }

    public void testPoolsizeIncreases() {
        int oldpoolsize = 10;
        newStartedThreadpool(oldpoolsize);

        int newpoolsize = 20;
        setDesiredPoolSize(newpoolsize);

        assertIsStarted();
        assertActualPoolsize(newpoolsize);
        assertDesiredPoolsize(newpoolsize);
        threadPoolThreadFactory.assertCreatedCount(newpoolsize);
    }

    public void testPoolsizeDecreases_IdleWorkersAreKilled() {
        int oldpoolsize = 20;
        newStartedThreadpool(oldpoolsize);

        int newpoolsize = 10;
        setDesiredPoolSize(newpoolsize);

        giveOthersAChance();
        assertIsStarted();
        assertActualPoolsize(newpoolsize);
        assertDesiredPoolsize(newpoolsize);
        threadPoolThreadFactory.assertCreatedCount(oldpoolsize);
    }

    public void testPoolsizeDecreases_ActiveWorkersAreKilledAfterWorking() {
        int oldPoolsize = 10;
        newStartedThreadpool(oldPoolsize);

        //let all workers work
        ensureNoIdleWorkers(2 * DELAY_MEDIUM_MS);
        sleepMs(DELAY_SMALL_MS);
        assertTrue(taskQueue.isEmpty());

        //change the size of the pool, but workers don't give workers time to pick it up
        int newPoolsize = 2;
        SetDesiredPoolsizeThread setSizeThread = scheduleSetDesiredPoolsize(newPoolsize);
        joinAll(setSizeThread);
        assertIsStarted();
        assertActualPoolsize(oldPoolsize);
        assertDesiredPoolsize(newPoolsize);

        //give the workers enough time to terminate
        sleepMs(DELAY_MEDIUM_MS * 3);
        assertIsStarted();
        assertActualPoolsize(newPoolsize);
        assertDesiredPoolsize(newPoolsize);
        threadPoolThreadFactory.assertCreatedCount(oldPoolsize);
    }

    public void testWhileUnstarted() {
        int oldPoolsize = 2;
        newUnstartedThreadPool(oldPoolsize);

        int newPoolsize = 10;
        setDesiredPoolSize(newPoolsize);
        assertIsUnstarted();
        assertActualPoolsize(0);
        assertDesiredPoolsize(newPoolsize);
        threadPoolThreadFactory.assertCreatedCount(0);
    }

    public void testWhileShuttingDown() {
        newShuttingdownThreadpool(10, DELAY_EON_MS);
        assertSetDesiredPoolSizeIsRejected();
    }

    public void testWhileShutdown() {
        newShutdownThreadpool();
        assertSetDesiredPoolSizeIsRejected();
    }

    private void assertSetDesiredPoolSizeIsRejected() {
        int oldThreadcount = threadPoolThreadFactory.getThreadCount();
        ThreadPoolState oldState = threadpool.getState();

        int desiredPoolsize = 10;
        SetDesiredPoolsizeThread setSizeThread = scheduleSetDesiredPoolsize(desiredPoolsize);

        joinAll(setSizeThread);
        setSizeThread.assertIsTerminatedWithThrowing(IllegalStateException.class);
        assertEquals(oldState, threadpool.getState());
        assertActualPoolsize(oldThreadcount);
        assertDesiredPoolsize(oldThreadcount);
        threadPoolThreadFactory.assertCreatedCount(oldThreadcount);
    }

    private void setDesiredPoolSize(int newpoolsize) {
        SetDesiredPoolsizeThread setSizeThread = scheduleSetDesiredPoolsize(newpoolsize);

        joinAll(setSizeThread);
        setSizeThread.assertIsTerminatedWithoutThrowing();
    }
}
