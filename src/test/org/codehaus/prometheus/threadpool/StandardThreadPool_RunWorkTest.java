package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.testsupport.DummyCallable;
import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;

public class StandardThreadPool_RunWorkTest extends StandardThreadPool_AbstractTest{

    public void testWhileRunning_returnsFalse() throws InterruptedException {
        int poolsize = 4;
        newStartedThreadpool(4);

        taskQueue.put(new DummyCallable<Boolean>(false));

        giveOthersAChance(DELAY_MEDIUM_MS);
        assertIsRunning();
        assertDesiredPoolsize(poolsize);
        assertActualPoolsize(poolsize-1);
        threadPoolThreadFactory.assertAliveCount(poolsize-1);
        threadPoolThreadFactory.assertTerminatedCount(1);
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileRunning_returnsTrue() throws InterruptedException {
        int poolsize = 4;
        newStartedThreadpool(4);

        taskQueue.put(new DummyCallable<Boolean>(true));

        giveOthersAChance(DELAY_MEDIUM_MS);
        assertIsRunning();
        assertDesiredPoolsize(poolsize);
        assertActualPoolsize(poolsize);
        threadPoolThreadFactory.assertAliveCount(poolsize);
        threadPoolThreadFactory.assertTerminatedCount(0);
        threadPoolExceptionHandler.assertNoErrors();
    }

    public void testWhileRunning_throwsUncheckedException(){

    }

    public void testWhileRunning_throwsCheckedException(){

    }

    public void testWhileShuttingDown_returnsFalse(){

    }

    public void testWhileShuttingDown_returnTrue(){

    }

    //exceptions: whileshuttingdowncheckedexception/whileshuttingdownuncheckedexception
}
