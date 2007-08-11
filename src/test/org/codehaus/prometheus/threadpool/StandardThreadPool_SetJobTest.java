/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.joinAll;
import org.codehaus.prometheus.testsupport.Delays;

/**
 * Unittests the {@link StandardThreadPool#setJob(ThreadPoolJob)} method.
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool_SetJobTest extends StandardThreadPool_AbstractTest {

    public void testArgument() {
        newUnstartedThreadPool();

        try {
            threadpool.setJob(null);
            fail();
        } catch (NullPointerException ex) {
        }
    }

    public void testWhileUnstarted() {
        newUnstartedThreadPool();

        ThreadPoolJob firstJob = new DummyThreadPoolJob();
        SetDefaultWorkerJobThread setThread = scheduleSetDefaultWorkerJob(firstJob);
        joinAll(setThread);

        setThread.assertIsTerminatedNormally();
        //make sure that the threadpool hasn't running.
        assertIsUnstarted();
        assertSame(firstJob, threadpool.getWorkerJob());

        //try to set another job after the first has been set, should succeed.
        ThreadPoolJob secondJob = new DummyThreadPoolJob();
        setThread = scheduleSetDefaultWorkerJob(secondJob);

        joinAll(setThread);
        setThread.assertIsTerminatedNormally();
        assertIsUnstarted();
        assertSame(secondJob, threadpool.getWorkerJob());
    }

    public void testWhileStarted() {
        newStartedThreadpool(10);
        assertSetDefaultWorkerJobIsRejected();
    }

    public void testWhileShuttingdown() {
        newShuttingdownThreadpool(10, Delays.EON_MS);
        assertSetDefaultWorkerJobIsRejected();
    }

    public void testWhileForcedShuttingdown() {
        newForcedShuttingdownThreadpool(3, Delays.LONG_MS);
        assertSetDefaultWorkerJobIsRejected();
    }

    public void testWhileShutdown() {
        newShutdownThreadpool();
        assertSetDefaultWorkerJobIsRejected();
    }

    private void assertSetDefaultWorkerJobIsRejected() {
        ThreadPoolJob oldjob = threadpool.getWorkerJob();

        ThreadPoolJob newjob = new DummyThreadPoolJob();
        SetDefaultWorkerJobThread setThread = scheduleSetDefaultWorkerJob(newjob);
        joinAll(setThread);

        setThread.assertIsTerminatedWithThrowing(IllegalStateException.class);
        assertSame(oldjob, threadpool.getWorkerJob());
    }
}
