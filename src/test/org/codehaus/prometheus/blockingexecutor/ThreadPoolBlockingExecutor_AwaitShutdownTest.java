/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import static org.codehaus.prometheus.concurrenttesting.TestSupport.newUninterruptableSleepingRunnable;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.joinAll;
import org.codehaus.prometheus.concurrenttesting.Delays;

/**
 * Unittests {@link ThreadPoolBlockingExecutor#awaitShutdown()}.
 *
 * @author Peter Veentjer
 */
public class ThreadPoolBlockingExecutor_AwaitShutdownTest extends ThreadPoolBlockingExecutor_AbstractTest {

    public void testWhileUnstarted() {
        newUnstartedBlockingExecutor(1, 1);
        AwaitShutdownThread waiter1Thread = scheduleAwaitShutdown();
        AwaitShutdownThread waiter2Thread = scheduleAwaitShutdown();

        //make sure that all waiters are waiting
        giveOthersAChance();
        waiter1Thread.assertIsStarted();
        waiter2Thread.assertIsStarted();

        spawned_shutdownPolitly();

        //make sure that all waiters have terminated without problems
        giveOthersAChance();
        waiter1Thread.assertIsTerminatedNormally();
        waiter2Thread.assertIsTerminatedNormally();
        assertIsShutdown();
    }

    public void testWhileRunning() {
        newStartedBlockingExecutor(1, 1);
        AwaitShutdownThread waiter1Thread = scheduleAwaitShutdown();
        AwaitShutdownThread waiter2Thread = scheduleAwaitShutdown();

        //make sure that all waiters are waiting
        giveOthersAChance();
        waiter1Thread.assertIsStarted();
        waiter2Thread.assertIsStarted();

        spawned_shutdownPolitly();

        //make sure that all waiters have terminated without problems
        giveOthersAChance();
        waiter1Thread.assertIsTerminatedNormally();
        waiter2Thread.assertIsTerminatedNormally();
        assertIsShutdown();
    }

    public void testWhileShuttingdown() {
        newShuttingdownBlockingExecutor(Delays.LONG_MS);
        AwaitShutdownThread waiter1Thread = scheduleAwaitShutdown();
        AwaitShutdownThread waiter2Thread = scheduleAwaitShutdown();

        //make sure that all waiters are waiting
        giveOthersAChance();
        waiter1Thread.assertIsStarted();
        waiter2Thread.assertIsStarted();

        //wait for the waiters to complete
        joinAll(waiter1Thread, waiter2Thread);
        waiter1Thread.assertIsTerminatedNormally();
        waiter2Thread.assertIsTerminatedNormally();
        assertIsShutdown();
    }

    public void testWhileForcedShuttingdown(){
        newForcedShuttingdownBlockingExecutor(Delays.LONG_MS,3);

        AwaitShutdownThread waiter1Thread = scheduleAwaitShutdown();
        AwaitShutdownThread waiter2Thread = scheduleAwaitShutdown();

        //make sure that all waiters are waiting
        giveOthersAChance();
        waiter1Thread.assertIsStarted();
        waiter2Thread.assertIsStarted();

        //wait for the waiters to complete
        joinAll(waiter1Thread, waiter2Thread);
        waiter1Thread.assertIsTerminatedNormally();
        waiter2Thread.assertIsTerminatedNormally();
        assertIsShutdown();
    }

    public void testWhileShutdown() {
        newShutdownBlockingExecutor(1, 1);
        AwaitShutdownThread waiter1Thread = scheduleAwaitShutdown();
        AwaitShutdownThread waiter2Thread = scheduleAwaitShutdown();

        //make sure all waiters finish without problems
        giveOthersAChance();
        waiter1Thread.assertIsTerminatedNormally();
        waiter2Thread.assertIsTerminatedNormally();
        assertIsShutdown();
    }

    public void testSomeWaitingNeeded() {
        newStartedBlockingExecutor(1, 1, newUninterruptableSleepingRunnable(Delays.LONG_MS));
        AwaitShutdownThread waiter1Thread = scheduleAwaitShutdown();
        AwaitShutdownThread waiter2Thread = scheduleAwaitShutdown();

        //make sure that all waiters are waiting
        giveOthersAChance();
        waiter1Thread.assertIsStarted();
        waiter2Thread.assertIsStarted();

        spawned_shutdownPolitly();
                
        //wait for the shutdown to complete and make sure that the waiters have completed
        joinAll(waiter1Thread, waiter2Thread);
        waiter1Thread.assertIsTerminatedNormally();
        waiter2Thread.assertIsTerminatedNormally();
        assertIsShutdown();
    }

    public void testInterruptedWhileWaiting() {
        newShuttingdownBlockingExecutor(Delays.EON_MS);

        //make sure that all waiters are waiting
        AwaitShutdownThread waiter1Thread = scheduleAwaitShutdown();
        AwaitShutdownThread waiter2Thread = scheduleAwaitShutdown();
        giveOthersAChance();
        waiter1Thread.assertIsStarted();
        waiter2Thread.assertIsStarted();

        //interrupt waiter1 and make sure that it is terminated and waiter2 is still waiting
        waiter1Thread.interrupt();
        giveOthersAChance(Delays.MEDIUM_MS);
        waiter1Thread.assertIsTerminatedByInterruptedException();
        waiter2Thread.assertIsStarted();
    }    
}
