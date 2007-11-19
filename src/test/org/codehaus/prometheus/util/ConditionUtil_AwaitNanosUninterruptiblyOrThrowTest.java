/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.*;
import org.codehaus.prometheus.concurrenttesting.Delays;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionUtil_AwaitNanosUninterruptiblyOrThrowTest extends ConditionUtil_AbstractTest {

    public void testArguments() throws TimeoutException {
        try {
            ConditionUtil.awaitNanosUninterruptiblyOrThrow(null, 10);
            fail();
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testNegativeTimeout() {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        lock.lock();
        try {
            ConditionUtil.awaitNanosUninterruptiblyOrThrow(condition, -1);
            fail();
        } catch (TimeoutException e) {
            assertTrue(true);
        } finally {
            lock.unlock();
        }
    }

    //===========================================================

    public void testSomeWaitingNeeded_startInterrupted() {
        testSomeWaitingNeeded(true);
    }

    public void testSomeWaitingNeeded_startUninterrupted() {
        testSomeWaitingNeeded(false);
    }

    public void testSomeWaitingNeeded(boolean interrupted) {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        long timeoutNs = millisToNanos(Delays.MEDIUM_MS);
        AwaitNanosUninterruptiblyOrThrowThread t = scheduleAwaitNanosUninterruptiblyOrThrow(lock, condition, timeoutNs, interrupted);

        giveOthersAChance();
        t.assertIsStarted();

        Thread signalThread = scheduleSignallAll(lock, condition);
        joinAll(signalThread, t);

        t.assertSuccess();
        t.assertIsTerminatedWithInterruptFlag(interrupted);
    }

    //===========================================================

    public void testTooMuchWaiting_startInterrupted() {
        testTooMuchWaiting(true);
    }

    public void testTooMuchWaiting_startUninterrupted() {
        testTooMuchWaiting(false);
    }

    public void testTooMuchWaiting(boolean startInterrupted) {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        long timeoutNs = millisToNanos(Delays.SMALL_MS);
        AwaitNanosUninterruptiblyOrThrowThread t = scheduleAwaitNanosUninterruptiblyOrThrow(lock, condition, timeoutNs, startInterrupted);
        joinAll(t);

        t.assertIsTimedOut();
        t.assertIsTerminatedWithInterruptFlag(startInterrupted);
    }

    //===========================================================

    public void testInterruptedWhileWaiting_startInterrupted() {
        testInterruptedWhileWaiting(true);
    }

    public void testInterruptedWhileWaiting_startUninterrupted() {
        testInterruptedWhileWaiting(false);
    }

    public void testInterruptedWhileWaiting(boolean interrupted) {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        long timeoutNs = millisToNanos(Delays.MEDIUM_MS);

        AwaitNanosUninterruptiblyOrThrowThread t = scheduleAwaitNanosUninterruptiblyOrThrow(lock, condition, timeoutNs, interrupted);

        giveOthersAChance();
        t.assertIsStarted();

        t.interrupt();
        giveOthersAChance();
        Thread signalThread = scheduleSignallAll(lock, condition);
        joinAll(t, signalThread);
        t.assertSuccess();
        t.assertIsTerminatedWithInterruptFlag();
    }
}
