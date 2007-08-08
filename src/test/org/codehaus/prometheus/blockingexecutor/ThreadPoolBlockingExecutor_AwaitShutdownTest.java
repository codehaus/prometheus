/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;
import org.codehaus.prometheus.testsupport.UninterruptableSleepingRunnable;

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

        spawned_assertShutdown();

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

        spawned_assertShutdown();

        //make sure that all waiters have terminated without problems
        giveOthersAChance();
        waiter1Thread.assertIsTerminatedNormally();
        waiter2Thread.assertIsTerminatedNormally();
        assertIsShutdown();
    }

    public void testWhileShuttingdown() {
        newShuttingdownBlockingExecutor(DELAY_LONG_MS);
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
        newForcedShuttingdownBlockingExecutor(DELAY_LONG_MS,3);

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
        newStartedBlockingExecutor(1, 1, new UninterruptableSleepingRunnable(DELAY_LONG_MS));
        AwaitShutdownThread waiter1Thread = scheduleAwaitShutdown();
        AwaitShutdownThread waiter2Thread = scheduleAwaitShutdown();

        //make sure that all waiters are waiting
        giveOthersAChance();
        waiter1Thread.assertIsStarted();
        waiter2Thread.assertIsStarted();

        spawned_assertShutdown();
                
        //wait for the shutdown to complete and make sure that the waiters have completed
        joinAll(waiter1Thread, waiter2Thread);
        waiter1Thread.assertIsTerminatedNormally();
        waiter2Thread.assertIsTerminatedNormally();
        assertIsShutdown();
    }

    public void testInterruptedWhileWaiting() {
        newShuttingdownBlockingExecutor(DELAY_EON_MS);

        //make sure that all waiters are waiting
        AwaitShutdownThread waiter1Thread = scheduleAwaitShutdown();
        AwaitShutdownThread waiter2Thread = scheduleAwaitShutdown();
        giveOthersAChance();
        waiter1Thread.assertIsStarted();
        waiter2Thread.assertIsStarted();

        //interrupt waiter1 and make sure that it is terminated and waiter2 is still waiting
        waiter1Thread.interrupt();
        giveOthersAChance(DELAY_MEDIUM_MS);
        waiter1Thread.assertIsTerminatedByInterruptedException();
        waiter2Thread.assertIsStarted();
    }    
}
