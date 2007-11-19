/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.concurrenttesting.TestCallable;
import org.codehaus.prometheus.concurrenttesting.Delays;
import static org.codehaus.prometheus.concurrenttesting.TestSupport.newDummyCallable;
import static org.codehaus.prometheus.concurrenttesting.TestSupport.newThrowingCallable;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;

import java.awt.image.ImagingOpException;
import java.io.IOException;

/**
 *
 */
public class StandardThreadPool_RunWorkTest extends StandardThreadPool_AbstractTest{

    //takeWork and getWorkForShutdown have to be tested

    public void testWhileRunning_returnsFalse() throws InterruptedException {
        int poolsize = 4;
        newStartedThreadpool(poolsize);

        workQueue.put(newDummyCallable(false));

        giveOthersAChance(Delays.MEDIUM_MS);

        assertWorkQueueIsEmpty();
        assertIsRunning();
        assertDesiredPoolsize(poolsize);
        assertActualPoolsize(poolsize-1);
        threadPoolThreadFactory.assertAliveCount(poolsize-1);
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

    public void _testWhileRunning_throwsUncheckedException() throws InterruptedException {
        testWhileRunning_throwsException(new ImagingOpException("foo"));
    }

    public void _testWhileRunning_throwsCheckedException() throws InterruptedException {
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
        threadPoolExceptionHandler.assertErrorCountAndNoOthers(ex.getClass(),1);
    }

    public void testWhileShuttingDown_returnsFalse(){

    }

    public void testWhileShuttingDown_returnTrue(){

    }

    public void _testWhileShuttingdown_throwsUncheckedException() throws InterruptedException {
        testWhileShuttingdown_throwsException(new ImagingOpException("foo"));
    }

    public void _testWhileShuttingdown_throwsCheckedException() throws InterruptedException {
        testWhileShuttingdown_throwsException(new IOException());
    }

    public void testWhileShuttingdown_throwsException(Exception ex) throws InterruptedException {
        int poolsize = 1;
        newShuttingdownThreadpool(poolsize, Delays.EON_MS);

        workQueue.put(newThrowingCallable(ex));
        //the trailing callable is placed to make sure that the threadpool isn't broken
        TestCallable<Boolean> trailingCallable = newDummyCallable(Boolean.TRUE);
        workQueue.put(trailingCallable);

        giveOthersAChance(Delays.MEDIUM_MS);

        assertWorkQueueIsEmpty();
        assertIsShuttingdown();
        assertDesiredPoolsize(poolsize);
        assertActualPoolsize(poolsize);
        trailingCallable.assertExecutedOnce();
        threadPoolThreadFactory.assertAliveCount(poolsize);
        threadPoolThreadFactory.assertNotAliveCount(0);
        threadPoolExceptionHandler.assertErrorCountAndNoOthers(ex.getClass(),1);
    }

    //exceptions: whileshuttingdowncheckedexception/whileshuttingdownuncheckedexception
}
