package org.codehaus.prometheus.threadpool;

import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;
import static org.codehaus.prometheus.testsupport.TestUtil.sleepMs;

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

        spawned_setDesiredPoolSize(poolsize);

        assertIsRunning();
        assertActualPoolsize(poolsize);
        assertDesiredPoolsize(poolsize);
        threadPoolThreadFactory.assertCreatedCount(poolsize);
        threadPoolThreadFactory.assertAllThreadsAlive();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testPoolsizeIncreases() {
        int oldpoolsize = 10;
        newStartedThreadpool(oldpoolsize);

        int newpoolsize = 20;
        spawned_setDesiredPoolSize(newpoolsize);

        assertIsRunning();
        assertActualPoolsize(newpoolsize);
        assertDesiredPoolsize(newpoolsize);
        threadPoolThreadFactory.assertCreatedCount(newpoolsize);
        threadPoolThreadFactory.assertAllThreadsAlive();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testPoolsizeDecreases_IdleWorkersAreKilled() {
        int oldpoolsize = 20;
        newStartedThreadpool(oldpoolsize);

        int newpoolsize = 10;
        spawned_setDesiredPoolSize(newpoolsize);

        giveOthersAChance();
        assertIsRunning();
        assertActualPoolsize(newpoolsize);
        assertDesiredPoolsize(newpoolsize);
        threadPoolThreadFactory.assertCreatedCount(oldpoolsize);
        threadPoolThreadFactory.assertAliveCount(newpoolsize);
        threadPoolThreadFactory.assertTerminatedCount(oldpoolsize-newpoolsize);
        threadPoolExceptionHandler.assertNoErrors();
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
        assertIsRunning();
        assertActualPoolsize(oldPoolsize);
        assertDesiredPoolsize(newPoolsize);

        //give the workers enough time to terminate
        sleepMs(DELAY_MEDIUM_MS * 3);
        assertIsRunning();
        assertActualPoolsize(newPoolsize);
        assertDesiredPoolsize(newPoolsize);
        threadPoolThreadFactory.assertCreatedCount(oldPoolsize);
        threadPoolThreadFactory.assertAliveCount(newPoolsize);
        threadPoolThreadFactory.assertTerminatedCount(oldPoolsize-newPoolsize);
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileUnstarted() {
        int oldPoolsize = 2;
        newUnstartedThreadPool(oldPoolsize);

        int newPoolsize = 5;
        spawned_setDesiredPoolSize(newPoolsize);
        assertIsUnstarted();
        assertActualPoolsize(0);
        assertDesiredPoolsize(newPoolsize);
        threadPoolThreadFactory.assertNoThreadsCreated();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileShuttingDown() {
        newShuttingdownThreadpool(3, DELAY_EON_MS);
        assertSetDesiredPoolSizeIsRejected();
    }

    public void testWhileForcedShuttingdown(){
        newForcedShuttingdownThreadpool(3,DELAY_LONG_MS);
        assertSetDesiredPoolSizeIsRejected();
    }

    public void testWhileShutdown() {
        newShutdownThreadpool();
        assertSetDesiredPoolSizeIsRejected();
    }

    private void assertSetDesiredPoolSizeIsRejected() {
        int oldThreadcount = threadPoolThreadFactory.getThreadCount();
        ThreadPoolState oldState = threadpool.getState();

        int desiredPoolsize = oldThreadcount+3;

        SetDesiredPoolsizeThread setSizeThread = scheduleSetDesiredPoolsize(desiredPoolsize);
        joinAll(setSizeThread);
        setSizeThread.assertIsTerminatedWithThrowing(IllegalStateException.class);

        assertEquals(oldState, threadpool.getState());
        assertActualPoolsize(oldThreadcount);
        assertDesiredPoolsize(oldThreadcount);
        threadPoolThreadFactory.assertCreatedCount(oldThreadcount);
        threadPoolThreadFactory.assertAllThreadsAlive();
        threadPoolExceptionHandler.assertNoErrors();
    }

    private void spawned_setDesiredPoolSize(int newpoolsize) {
        SetDesiredPoolsizeThread t = scheduleSetDesiredPoolsize(newpoolsize);
        joinAll(t);
        t.assertIsTerminatedNormally();
    }
}
