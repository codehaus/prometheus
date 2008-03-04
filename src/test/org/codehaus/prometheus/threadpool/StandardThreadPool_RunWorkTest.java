/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;
import org.codehaus.prometheus.concurrenttesting.Delays;
import org.codehaus.prometheus.concurrenttesting.TestCallable;
import static org.codehaus.prometheus.concurrenttesting.TestSupport.newDummyCallable;
import static org.codehaus.prometheus.concurrenttesting.TestSupport.newThrowingCallable;

import java.awt.image.ImagingOpException;
import java.io.IOException;

/**
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool_RunWorkTest extends StandardThreadPool_AbstractTest {

    public void testWhileRunning_returnsFalse() throws InterruptedException {
        int poolsize = 4;
        newStartedThreadpool(poolsize);

        workQueue.put(newDummyCallable(false));

        giveOthersAChance(Delays.MEDIUM_MS);

        assertWorkQueueIsEmpty();
        assertIsRunning();
        assertDesiredPoolsize(poolsize);
        assertActualPoolsize(poolsize - 1);
        threadPoolThreadFactory.assertAliveCount(poolsize - 1);
        threadPoolThreadFactory.assertNotAliveCount(1);
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileRunning_returnsTrue() throws InterruptedException {
        int poolsize = 4;
        newStartedThreadpool(poolsize);

        workQueue.put(newDummyCallable(true));

        giveOthersAChance(Delays.MEDIUM_MS);

        assertWorkQueueIsEmpty();
        assertIsRunning();
        assertDesiredPoolsize(poolsize);
        assertActualPoolsize(poolsize);
        threadPoolThreadFactory.assertAliveCount(poolsize);
        threadPoolThreadFactory.assertNotAliveCount(0);
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileRunning_throwsUncheckedException() throws InterruptedException {
        testWhileRunning_throwsException(new ImagingOpException("foo"));
    }

    public void testWhileRunning_throwsCheckedException() throws InterruptedException {
        testWhileRunning_throwsException(new IOException());
    }

    public void testWhileRunning_throwsException(Exception ex) throws InterruptedException {
        int poolsize = 1;
        newStartedThreadpool(poolsize);

        workQueue.put(newThrowingCallable(ex));
        //the trailing callable is placed to make sure that the threadpool isn't broken
        TestCallable<Boolean> trailingCallable = new TestCallable<Boolean>(Boolean.TRUE);
        workQueue.put(trailingCallable);

        giveOthersAChance(Delays.MEDIUM_MS);

        assertWorkQueueIsEmpty();
        assertIsRunning();
        assertDesiredPoolsize(poolsize);
        assertActualPoolsize(poolsize);
        trailingCallable.assertExecutedOnce();
        threadPoolThreadFactory.assertAliveCount(poolsize);
        threadPoolThreadFactory.assertNotAliveCount(0);
        threadPoolExceptionHandler.assertErrorCountAndNoOthers(ex.getClass(), 1);
    }

    public void testWhileShuttingDown_returnsFalse() {
        newShuttingdownThreadpool(1,Delays.LONG_MS);

        fail();
    }

    public void testWhileShuttingDown_returnTrue() {
        fail();
    }

    public void testWhileShuttingdown_throwsUncheckedException() throws InterruptedException {
        testWhileShuttingdown_throwsException(new ImagingOpException("foo"));
    }

    public void testWhileShuttingdown_throwsCheckedException() throws InterruptedException {
        testWhileShuttingdown_throwsException(new IOException());
    }

    public void testWhileShuttingdown_throwsException(Exception ex) throws InterruptedException {
        newShuttingdownThreadpool(1, Delays.LONG_MS);

        TestCallable problemCausingCallable = new TestCallable(ex);
        workQueue.put(problemCausingCallable);

        //the trailing callable is placed to make sure that the threadpool isn't broken
        TestCallable<Boolean> trailingCallable = newDummyCallable(Boolean.TRUE);
        workQueue.put(trailingCallable);

        spawnNewWorker();

        giveOthersAChance(Delays.MEDIUM_MS);

        assertIsShuttingdown();

        assertWorkQueueIsEmpty();
        assertDesiredPoolsize(1);
        assertActualPoolsize(1);
        trailingCallable.assertExecutedOnce();
        threadPoolThreadFactory.assertCreatedCount(2);
        threadPoolThreadFactory.assertAliveCount(1);
        threadPoolThreadFactory.assertNotAliveCount(1);
        threadPoolExceptionHandler.assertErrorCountAndNoOthers(ex.getClass(), 1);
    }
}
