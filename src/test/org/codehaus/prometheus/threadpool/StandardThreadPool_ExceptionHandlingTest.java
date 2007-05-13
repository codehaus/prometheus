package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.exceptionhandler.NullExceptionHandler;
import org.codehaus.prometheus.testsupport.ThrowingRunnable;

/**
 * Unittests the {@link StandardThreadPool} {@link org.codehaus.prometheus.exceptionhandler.ExceptionHandler}
 * functionality.
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool_ExceptionHandlingTest extends StandardThreadPool_AbstractTest {

    public void testSetNullExceptionHandler() {
        newStartedThreadpool();

        threadpool.setExceptionHandler(null);
        assertTrue(threadpool.getExceptionHandler() instanceof NullExceptionHandler);
    }

    //every time the task is executed, an exception is thrown.
    //it also tests that an exception doesn't corrupt the threadpool (so workers work
    //again after receiving an exception
    public void testHandlerIsCalled() {
        int poolsize = 2;
        newStartedThreadpool(poolsize);

        int errorcount = 30;
        for(int k=0;k<errorcount;k++)        
            taskQueue.add(new ThrowingRunnable());

        giveOthersAChance();
        assertActualPoolsize(poolsize);
        assertIsStarted();
        threadPoolExceptionHandler.assertCount(errorcount);
    }

    public void testHandlerIsNotCalledForIdleWorkerThatIsInterrupted() {
        newStartedThreadpool(10);

        //all workers are now idle because no work is in the taskqueue.
        //shut down the
        ShutdownNowThread shutdownThread = scheduleShutdownNow();
        joinAll(shutdownThread);
        shutdownThread.assertIsTerminatedWithoutThrowing();

        //make sure that no exception has been thrown.
        giveOthersAChance();
        threadPoolExceptionHandler.assertCount(0);
    }

    public void testSet_whileUnstarted() {
        newUnstartedThreadPool();
        assertSetHandlerWorks();
    }

    public void testSet_whileStarted() {
        newStartedThreadpool();
        assertSetHandlerWorks();
    }

    public void testSet_whileShuttingDown() {
        newShuttingdownThreadpool(10,DELAY_EON_MS);
        assertSetHandlerWorks();
    }

    public void assertSetHandlerWorks() {
        ExceptionHandler handler = new NullExceptionHandler();
        threadpool.setExceptionHandler(handler);
        assertSame(handler, threadpool.getExceptionHandler());
    }
}
