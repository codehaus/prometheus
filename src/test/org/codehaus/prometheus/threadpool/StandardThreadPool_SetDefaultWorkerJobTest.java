package org.codehaus.prometheus.threadpool;

/**
 * Unittests the {@link StandardThreadPool#setWorkerJob(WorkerJob)} method.
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool_SetDefaultWorkerJobTest extends StandardThreadPool_AbstractTest {

    public void testArgument() {
        newUnstartedThreadPool();

        try {
            threadpool.setWorkerJob(null);
            fail();
        } catch (NullPointerException ex) {
        }
    }

    public void testWhileUnstarted() {
        newUnstartedThreadPool();

        WorkerJob firstJob = new DummyWorkerJob();
        SetDefaultWorkerJobThread setThread = scheduleSetDefaultWorkerJob(firstJob);
        joinAll(setThread);

        setThread.assertIsTerminatedNormally();
        //make sure that the threadpool hasn't started.
        assertIsUnstarted();
        assertSame(firstJob, threadpool.getDefaultWorkerJob());

        //try to set another job after the first has been set, should succeed.
        WorkerJob secondJob = new DummyWorkerJob();
        setThread = scheduleSetDefaultWorkerJob(secondJob);

        joinAll(setThread);
        setThread.assertIsTerminatedNormally();
        assertIsUnstarted();
        assertSame(secondJob, threadpool.getDefaultWorkerJob());
    }

    public void testWhileStarted() {
        newStartedThreadpool(10);
        assertSetDefaultWorkerJobIsRejected();
    }

    public void testWhileShuttingdown() {
        newShuttingdownThreadpool(10, DELAY_EON_MS);
        assertSetDefaultWorkerJobIsRejected();
    }

    public void testWhileShutdown() {
        newShutdownThreadpool();
        assertSetDefaultWorkerJobIsRejected();
    }

    private void assertSetDefaultWorkerJobIsRejected() {
        WorkerJob oldjob = threadpool.getDefaultWorkerJob();

        WorkerJob newjob = new DummyWorkerJob();
        SetDefaultWorkerJobThread setThread = scheduleSetDefaultWorkerJob(newjob);
        joinAll(setThread);

        setThread.assertIsTerminatedWithThrowing(IllegalStateException.class);
        assertSame(oldjob, threadpool.getDefaultWorkerJob());
    }
}
