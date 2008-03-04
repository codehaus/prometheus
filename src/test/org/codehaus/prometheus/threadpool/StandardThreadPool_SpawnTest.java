package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.concurrenttesting.Delays;

public class StandardThreadPool_SpawnTest extends StandardThreadPool_AbstractTest {

    public void test_nullArgument() {
        newStartedThreadpool();

        try {
            threadpool.spawn(null);
            fail();
        } catch (NullPointerException ex) {
        }

        threadPoolThreadFactory.assertNoneCreated();
        threadPoolExceptionHandler.assertNoErrors();
        assertIsRunning();
    }

    public void test_whileUnstarted() {
        newUnstartedThreadPool();
        assertSpawnIsAccepted(ThreadPoolState.running);
    }

    public void test_whileShuttingdownPolitly() {
        newShuttingdownThreadpool(1, Delays.LONG_MS);
        assertSpawnIsAccepted(ThreadPoolState.shuttingdownnormally);
    }

    public void test_whileShuttingdownForced() {
        fail();
    }

    public void test_whileShutdown() {
        newShutdownThreadpool();
        assertSpawnCausesIllegalStateException();
    }

    public void assertSpawnCausesIllegalStateException() {
        ThreadPoolState oldState = threadpool.getState();
        int oldThreadcount = threadPoolThreadFactory.getThreadCount();

        ThreadPoolJob job = new TestThreadPoolJob();
        try {
            threadpool.spawn(job);
            fail();
        } catch (IllegalStateException ex) {
        }

        assertHasState(oldState);
        threadPoolThreadFactory.assertCreatedAndAliveCount(oldThreadcount);
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileRunning() {
        newStartedThreadpool();
        assertSpawnIsAccepted(ThreadPoolState.running);
    }

    private void assertSpawnIsAccepted(ThreadPoolState expectedState) {
        int oldCreatedCount = threadPoolThreadFactory.getThreadCount();

        threadpool.spawn(new TestThreadPoolJob());

        assertHasState(expectedState);
        threadPoolThreadFactory.assertCreatedAndAliveCount(oldCreatedCount+1);
        threadPoolExceptionHandler.assertNoErrors();
    }
}
