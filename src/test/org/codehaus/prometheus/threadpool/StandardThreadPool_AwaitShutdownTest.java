package org.codehaus.prometheus.threadpool;

/**
 * Unittests the {@link StandardThreadPool#awaitShutdown()} method.
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool_AwaitShutdownTest extends StandardThreadPool_AbstractTest{

    public void testWhileUnstarted(){
        newUnstartedThreadPool(10);
        assertShutdownTerminatesWaiters();
    }
    
    public void testWhileStarted(){
        newStartedThreadpool(10);
        assertShutdownTerminatesWaiters();
    }

    public void assertShutdownTerminatesWaiters(){
        AwaitShutdownThread awaitThread1 = scheduleAwaitShutdown();
        AwaitShutdownThread awaitThread2 = scheduleAwaitShutdown();
        sleepMs(DELAY_TINY_MS);

        awaitThread1.assertIsStarted();
        awaitThread2.assertIsStarted();

        //call the shutdown.
        ShutdownThread shutdownThread = scheduleShutdown();
        joinAll(shutdownThread);
        shutdownThread.assertIsTerminatedWithoutThrowing();

        //check that the awaiting threads shutdown
        joinAll(awaitThread1,awaitThread2);
        awaitThread1.assertIsTerminatedWithoutThrowing();
        awaitThread2.assertIsTerminatedWithoutThrowing();
        assertIsShutdown();
    }

    public void testWhileShuttingdown(){
        newShuttingdownThreadpool(3,DELAY_MEDIUM_MS);

        AwaitShutdownThread awaitThread1 = scheduleAwaitShutdown();
        AwaitShutdownThread awaitThread2 = scheduleAwaitShutdown();
        sleepMs(DELAY_TINY_MS);

        //check that the await wasn't successful immediately.
        awaitThread1.assertIsStarted();
        awaitThread2.assertIsStarted();

        //check that the awaits are successful after shutdown.
        joinAll(awaitThread1,awaitThread2);
        awaitThread1.assertIsTerminatedWithoutThrowing();
        awaitThread2.assertIsTerminatedWithoutThrowing();
        assertIsShutdown();
    }

    public void testInterruptedWhileWaiting(){
        newStartedThreadpool(10);

        AwaitShutdownThread awaitThread1 = scheduleAwaitShutdown();
        AwaitShutdownThread awaitThread2 = scheduleAwaitShutdown();
        sleepMs(DELAY_TINY_MS);

        //check that the await wasn't successful immediately.
        awaitThread1.assertIsStarted();
        awaitThread2.assertIsStarted();

        awaitThread1.interrupt();

        //interrupt thread1 and see that thread1 is interrupted, and thread2 still is waiting
        joinAll(awaitThread1);
        awaitThread1.assertIsInterruptedByException();
        awaitThread2.assertIsStarted();
        assertIsStarted();
    }

    public void testWhileShutdown(){
        newShutdownThreadpool();

        AwaitShutdownThread awaitThread1 = scheduleAwaitShutdown();
        AwaitShutdownThread awaitThread2 = scheduleAwaitShutdown();
        sleepMs(DELAY_TINY_MS);

        awaitThread1.assertIsTerminatedWithoutThrowing();
        awaitThread2.assertIsTerminatedWithoutThrowing();
        assertIsShutdown();
    }
}
