package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil;
import org.codehaus.prometheus.concurrenttesting.Delays;

public class StandardThreadPool_SpawnWithoutStartingTest extends StandardThreadPool_AbstractTest {

    public void testArguments_nullTask() {
        newStartedThreadpool();

        try {
            threadpool.spawnWithoutStarting(null, 1);
            fail();
        } catch (NullPointerException ex) {
        }

        assertHasState(ThreadPoolState.running);
        threadPoolThreadFactory.assertNoneCreated();
    }

    public void testArguments_negativeThreadcount() {
        newStartedThreadpool();

        try {
            threadpool.spawnWithoutStarting(new TestThreadPoolJob(), -1);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        assertIsRunning();
        threadPoolThreadFactory.assertNoneCreated();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testNoSpawnCount_WhileUnstarted() {
        newUnstartedThreadPool();

        TestThreadPoolJob job = new TestThreadPoolJob();
        threadpool.spawnWithoutStarting(job, 0);

        assertIsUnstarted();
        threadPoolThreadFactory.assertNoneCreated();
        threadPoolExceptionHandler.assertNoErrors();

        //lets start the threadpool and make sure no threads are created
        spawned_start();

        assertIsRunning();
        threadPoolThreadFactory.assertNoneCreated();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testNoSpawnCount_WhileStarted() {
        newStartedThreadpool();

        TestThreadPoolJob job = new TestThreadPoolJob();
        threadpool.spawnWithoutStarting(job, 0);

        assertIsRunning();
        threadPoolThreadFactory.assertNoneCreated();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testNoSpawnCount_WhileShuttingdown() {
        fail();
    }

    public void testNoSpawnCount_WhileShutdown() {
        newShutdownThreadpool();

        assertSpawnWithoutStartingCausesIllegalStateException(0);
    }

    public void testWhileUnstarted() {
        //create a new unstarted threadpool and register some initial spawns
        newUnstartedThreadPool();

        int threadcount1 = 3;
        threadpool.spawnWithoutStarting(new TestThreadPoolJob(), threadcount1);

        int threadcount2 = 2;
        threadpool.spawnWithoutStarting(new TestThreadPoolJob(), threadcount2);

        assertIsUnstarted();
        threadPoolThreadFactory.assertNoneCreated();

        //start the threadpool and see that the task have been started
        spawned_start();

        ConcurrentTestUtil.giveOthersAChance();

        assertIsRunning();
        threadPoolThreadFactory.assertCreatedCount(threadcount1 + threadcount2);
        threadPoolThreadFactory.assertAllAreAlive();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileRunning() {
        newStartedThreadpool();

        int threadcount1 = 3;
        threadpool.spawnWithoutStarting(new TestThreadPoolJob(), threadcount1);

        int threadcount2 = 4;
        threadpool.spawnWithoutStarting(new TestThreadPoolJob(), threadcount2);

        assertIsRunning();
        threadPoolThreadFactory.assertCreatedCount(threadcount1 + threadcount2);
        threadPoolThreadFactory.assertAllAreAlive();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileShuttingDown() {
        newShuttingdownThreadpool(1, Delays.LONG_MS);
        assertSpawnWithoutStartingCausesIllegalStateException(2);
    }

    public void testWhileShutdown() {
        newShutdownThreadpool();
        assertSpawnWithoutStartingCausesIllegalStateException(2);
    }

    private void assertSpawnWithoutStartingCausesIllegalStateException(int count) {
        ThreadPoolState oldState = threadpool.getState();
        int oldThreadCreatedCount = threadPoolThreadFactory.getThreadCount();

        DummyThreadPoolJob job = new DummyThreadPoolJob(threadpool);
        try {
            threadpool.spawnWithoutStarting(job, count);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertHasState(oldState);
        threadPoolThreadFactory.assertCreatedCount(oldThreadCreatedCount);
        threadPoolExceptionHandler.assertNoErrors();
        job.assertNoExecuteWork();
        job.assertNoTakeWork();
    }
}
