/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import static org.codehaus.prometheus.testsupport.TestUtil.scheduleSignallAll;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionUtil_TimedAwaitAndThrowTest extends ConditionUtil_AbstractTest {

    public static Condition createCondition() {
        return new ReentrantLock().newCondition();
    }

    public void testArguments() throws TimeoutException, InterruptedException {
        try {
            ConditionUtil.awaitAndThrow(null, 1, TimeUnit.SECONDS);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }

        try {
            ConditionUtil.awaitAndThrow(createCondition(), 1, null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testNegativeTimeout() throws InterruptedException {
        try {
            ConditionUtil.awaitAndThrow(createCondition(), -1, TimeUnit.NANOSECONDS);
        } catch (TimeoutException ex) {
            assertTrue(true);
        }
    }

    public void testInterruptedWhileWaiting() throws TimeoutException, InterruptedException {
        Lock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        AwaitAndThrowThread awaitThread = scheduleAwaitAndThrow(lock,cond, DELAY_LONG_MS);

        giveOthersAChance();
        awaitThread.assertIsStarted();

        awaitThread.interrupt();
        joinAll(awaitThread);
        awaitThread.assertIsInterruptedByException();
    }

    public void testSomeWaiting() throws InterruptedException, TimeoutException {
        Lock lock = new ReentrantLock();
        Condition cond = lock.newCondition();
        AwaitAndThrowThread awaitThread = scheduleAwaitAndThrow(lock,cond, DELAY_LONG_MS);

        giveOthersAChance();
        awaitThread.assertIsStarted();

        Thread signalAllThread = scheduleSignallAll(lock,cond);
        joinAll(signalAllThread,awaitThread);
        
        awaitThread.assertIsSuccess(DELAY_LONG_MS - DELAY_TINY_MS);
    }

    public void testTooMuchWaiting() throws InterruptedException {
        Lock lock = new ReentrantLock();
        Condition cond = lock.newCondition();

        AwaitAndThrowThread awaitThread = scheduleAwaitAndThrow(lock,cond, DELAY_MEDIUM_MS);
        joinAll(awaitThread);

        awaitThread.assertIsTimedOut();
    }

    public void testIllegalMonitorState() throws TimeoutException, InterruptedException {
        Lock lock = new ReentrantLock();
        Condition cond = lock.newCondition();

        try {
            ConditionUtil.awaitAndThrow(cond, DELAY_SMALL_MS, TimeUnit.MILLISECONDS);
            fail("IllegalMonitorStateException expected");
        } catch (IllegalMonitorStateException e) {
            assertTrue(true);
        }
    }
}
