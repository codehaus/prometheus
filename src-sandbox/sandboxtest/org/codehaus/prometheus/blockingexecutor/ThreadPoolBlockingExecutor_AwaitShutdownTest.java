/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.testsupport.NonInterruptableSleepingRunnable;

public class ThreadPoolBlockingExecutor_AwaitShutdownTest extends ThreadPoolBlockingExecutor_AbstractTest {

    public void testNotStarted() {
        newUnstartedBlockingExecutor(1,1);
        AwaitShutdownThread t1 = scheduleAwaitShutdown();
        AwaitShutdownThread t2 = scheduleAwaitShutdown();

        Thread.yield();
        t1.assertIsStarted();
        t2.assertIsStarted();

        Thread shutdownThread = scheduleShutdown();
        joinAll(shutdownThread);
        Thread.yield();

        t1.assertIsTerminated();
        t2.assertIsTerminated();
        assertIsShutdown();
    }

    public void testStarted() {
        newStartedBlockingExecutor(1,1);
        AwaitShutdownThread t1 = scheduleAwaitShutdown();
        AwaitShutdownThread t2 = scheduleAwaitShutdown();

        Thread.yield();
        t1.assertIsStarted();
        t2.assertIsStarted();

        Thread shutdownThread = scheduleShutdown();
        joinAll(shutdownThread);
        Thread.yield();

        t1.assertIsTerminated();
        t2.assertIsTerminated();
        assertIsShutdown();
    }

    public void testShuttingDown() {
        newShuttingDownBlockingExecutor(500);
        AwaitShutdownThread t1 = scheduleAwaitShutdown();
        AwaitShutdownThread t2 = scheduleAwaitShutdown();

        Thread.yield();
        t1.assertIsTerminated();
        t2.assertIsTerminated();

        joinAll(t1,t1);
        t1.assertIsTerminated();
        t2.assertIsTerminated();
        assertIsShutdown();
    }

    public void testShutdown() {
        newShutdownBlockingExecutor(1,1);
        AwaitShutdownThread t1 = scheduleAwaitShutdown();
        AwaitShutdownThread t2 = scheduleAwaitShutdown();

        Thread.yield();
        t1.assertIsTerminated();
        t2.assertIsTerminated();
        assertIsShutdown();
    }

    public void testSomeWaitingNeeded(){
        newStartedBlockingExecutor(1,1,new NonInterruptableSleepingRunnable(200));
        AwaitShutdownThread t1 = scheduleAwaitShutdown();
        AwaitShutdownThread t2 = scheduleAwaitShutdown();

        Thread.yield();
        t1.assertIsStarted();
        t2.assertIsStarted();

        Thread shutdownThread = scheduleShutdown();
        joinAll(shutdownThread);
        Thread.yield();

        t1.assertIsTerminated();
        t2.assertIsTerminated();
        assertIsShutdown();
    }

    public void testInterruptedWhileWaiting() {
        fail();
    }

    public void testSpuriousWakeup() {
        fail();
    }
}
