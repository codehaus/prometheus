package org.codehaus.prometheus.threadpool;

import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;
import org.codehaus.prometheus.concurrenttesting.Delays;

public class StandardThreadPool_SpawnWithThreadcountTest extends StandardThreadPool_AbstractTest {

    public void testArgument_illegalPoolsize() {
        newStartedThreadpool();

        DummyThreadPoolJob job = new DummyThreadPoolJob(threadpool);
        try {
            threadpool.spawn(job, -1);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        assertIsRunning();
        threadPoolThreadFactory.assertNoneCreated();
        threadPoolExceptionHandler.assertNoErrors();
        job.assertNoTakeWork();
        job.assertNoExecuteWork();
    }

    public void testArgument_nullTask() {
        newStartedThreadpool();

        try {
            threadpool.spawn(null, 1);
            fail();
        } catch (NullPointerException ex) {
        }

        assertIsRunning();
        threadPoolThreadFactory.assertNoneCreated();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testZeroSpawnCount_WhileUnstarted() {
        newUnstartedThreadPool();

        TestThreadPoolJob job = new TestThreadPoolJob();
        threadpool.spawn(job, 0);

        assertIsUnstarted();
        threadPoolExceptionHandler.assertNoErrors();
        threadPoolThreadFactory.assertNoneCreated();
    }

    public void testZeroSpawnCount_WhileStarted() {
        newStartedThreadpool();

        TestThreadPoolJob job = new TestThreadPoolJob();
        threadpool.spawn(job, 0);

        assertIsRunning();
        threadPoolThreadFactory.assertNoneCreated();
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testZeroSpawnCount_WhileShuttingdown() {
        fail();
    }

    public void testZeroSpawnCount_WhileShutdown() {
        testWhileShutdown(0);
    }

    public void testWhileShutdown() {
        testWhileShutdown(3);
    }

    public void testWhileUnstarted_noInitialSpawns() {
        newUnstartedThreadPool();
        assertSpawnIsSuccess(ThreadPoolState.running);
    }

    public void testWhileUnstarted_withInitialSpawns() {
        newUnstartedThreadPool();

        DummyThreadPoolJob initialJob = new DummyThreadPoolJob(threadpool);
        int initialSpawnCount = 3;
        threadpool.spawnWithoutStarting(initialJob,initialSpawnCount);

        DummyThreadPoolJob job = new DummyThreadPoolJob(threadpool);
        int spawnCount = 4;
        threadpool.spawn(job, spawnCount);

        giveOthersAChance(Delays.MEDIUM_MS);

        assertIsRunning();
        threadPoolThreadFactory.assertCreatedAndAliveCount(initialSpawnCount + spawnCount);
        threadPoolExceptionHandler.assertNoErrors();
        initialJob.assertHasMultipleTakeWork();
        initialJob.assertHasMultipleExecuteWork();
        job.assertHasMultipleTakeWork();
        job.assertHasMultipleExecuteWork();
    }

    public void testWhileRunning_emptyPool() {
        newStartedThreadpool();
        assertSpawnIsSuccess(ThreadPoolState.running);
    }

    public void testWhileRunning_nonEmptyPool() {
        newStartedThreadpool(3);
        assertSpawnIsSuccess(ThreadPoolState.running);
    }

    public void testWhileShuttingdown() {
        fail();
    }

    public void testWhileShuttingdownNormally() {
        fail();
    }

    private void assertSpawnIsSuccess(ThreadPoolState expectedState) {
        int oldCreatedCount = threadPoolThreadFactory.getAliveCount();
        DummyThreadPoolJob job = new DummyThreadPoolJob(threadpool);

        int threadCount = 4;
        threadpool.spawn(job, threadCount);

        giveOthersAChance(Delays.MEDIUM_MS);

        assertHasState(expectedState);
        threadPoolThreadFactory.assertCreatedAndAliveCount(threadCount + oldCreatedCount);
        threadPoolExceptionHandler.assertNoErrors();
        job.assertHasMultipleTakeWork();
        job.assertHasMultipleExecuteWork();
    }

    public void testWhileShutdown(int spawnCount) {
        newShutdownThreadpool();

        DummyThreadPoolJob job = new DummyThreadPoolJob(threadpool);

        try {
            threadpool.spawn(job, spawnCount);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertIsShutdown();
        threadPoolThreadFactory.assertNoneCreated();
        threadPoolExceptionHandler.assertNoErrors();
        job.assertNoTakeWork();
        job.assertNoExecuteWork();
    }

}
