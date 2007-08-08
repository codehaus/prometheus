/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.testsupport.CountingCallable;
import org.codehaus.prometheus.testsupport.TestCallable;
import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;
import org.codehaus.prometheus.testsupport.ThrowingCallable;

import java.awt.image.ImagingOpException;
import java.io.IOException;

/**
 *
 */
public class StandardThreadPool_RunWorkTest extends StandardThreadPool_AbstractTest{

    //getWork and getWorkForShutdown have to be tested

    public void testWhileRunning_returnsFalse() throws InterruptedException {
        int poolsize = 4;
        newStartedThreadpool(poolsize);

        workQueue.put(new TestCallable<Boolean>(false));

        giveOthersAChance(DELAY_MEDIUM_MS);

        assertWorkQueueIsEmpty();
        assertIsRunning();
        assertDesiredPoolsize(poolsize);
        assertActualPoolsize(poolsize-1);
        threadPoolThreadFactory.assertAliveCount(poolsize-1);
        threadPoolThreadFactory.assertTerminatedCount(1);
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileRunning_returnsTrue() throws InterruptedException {
        int poolsize = 4;
        newStartedThreadpool(poolsize);

        workQueue.put(new TestCallable<Boolean>(true));

        giveOthersAChance(DELAY_MEDIUM_MS);

        assertWorkQueueIsEmpty();
        assertIsRunning();
        assertDesiredPoolsize(poolsize);
        assertActualPoolsize(poolsize);
        threadPoolThreadFactory.assertAliveCount(poolsize);
        threadPoolThreadFactory.assertTerminatedCount(0);
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

        workQueue.put(new ThrowingCallable(ex));
        //the trailing callable is placed to make sure that the threadpool isn't broken
        CountingCallable trailingCallable = new CountingCallable(Boolean.TRUE);
        workQueue.put(trailingCallable);

        giveOthersAChance(DELAY_MEDIUM_MS);

        assertWorkQueueIsEmpty();
        assertIsRunning();
        assertDesiredPoolsize(poolsize);
        assertActualPoolsize(poolsize);
        trailingCallable.assertExecutedOnce();
        threadPoolThreadFactory.assertAliveCount(poolsize);
        threadPoolThreadFactory.assertTerminatedCount(0);
        threadPoolExceptionHandler.assertErrorCountAndNoOthers(ex.getClass(),1);
    }

    public void testWhileShuttingDown_returnsFalse(){

    }

    public void testWhileShuttingDown_returnTrue(){

    }

    public void testWhileShuttingdown_throwsUncheckedException() throws InterruptedException {
        testWhileShuttingdown_throwsException(new ImagingOpException("foo"));
    }

    public void testWhileShuttingdown_throwsCheckedException() throws InterruptedException {
        testWhileShuttingdown_throwsException(new IOException());
    }

    public void testWhileShuttingdown_throwsException(Exception ex) throws InterruptedException {
        int poolsize = 1;
        newShuttingdownThreadpool(poolsize, DELAY_EON_MS);

        workQueue.put(new ThrowingCallable(ex));
        //the trailing callable is placed to make sure that the threadpool isn't broken
        CountingCallable trailingCallable = new CountingCallable(Boolean.TRUE);
        workQueue.put(trailingCallable);

        giveOthersAChance(DELAY_MEDIUM_MS);

        assertWorkQueueIsEmpty();
        assertIsShuttingdown();
        assertDesiredPoolsize(poolsize);
        assertActualPoolsize(poolsize);
        trailingCallable.assertExecutedOnce();
        threadPoolThreadFactory.assertAliveCount(poolsize);
        threadPoolThreadFactory.assertTerminatedCount(0);
        threadPoolExceptionHandler.assertErrorCountAndNoOthers(ex.getClass(),1);
    }

    //exceptions: whileshuttingdowncheckedexception/whileshuttingdownuncheckedexception
}
