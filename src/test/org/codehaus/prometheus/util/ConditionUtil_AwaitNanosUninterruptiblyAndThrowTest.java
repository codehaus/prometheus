/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import org.codehaus.prometheus.testsupport.TestUtil;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionUtil_AwaitNanosUninterruptiblyAndThrowTest extends ConditionUtil_AbstractTest{

    public void testArguments() throws TimeoutException {
        try{
            ConditionUtil.awaitNanosUninterruptiblyAndThrow(null,10);
            fail();
        }catch(NullPointerException ex){
            assertTrue(true);
        }
    }

    public void testNegativeTimeout(){
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        lock.lock();
        try {
            ConditionUtil.awaitNanosUninterruptiblyAndThrow(condition,-1);
            fail();
        } catch (TimeoutException e) {
            assertTrue(true);
        }        
    }

    //===========================================================

    public void testSomeWaitingNeeded_startInterrupted(){
        testSomeWaitingNeeded(true);
    }

    public void testSomeWaitingNeeded_startUninterrupted(){
        testSomeWaitingNeeded(false);
    }

    public void testSomeWaitingNeeded(boolean interrupted){
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        long timeoutNs = millisToNanos(DELAY_MEDIUM_MS);
        AwaitNanosUninterruptiblyAndThrowThread t = scheduleAwaitNanosUninterruptiblyAndThrow(lock,condition,timeoutNs,interrupted);

        sleepMs(DELAY_TINY_MS);
        t.assertIsStarted();

        Thread signalThread = TestUtil.scheduleSignallAll(lock,condition);
        joinAll(signalThread,t);

        t.assertSuccess();
        t.assertIsTerminatedWithInterruptStatus(interrupted);
    }

    //===========================================================

    public void testTooMuchWaiting_startInterrupted(){
        testTooMuchWaiting(true);
    }

    public void testTooMuchWaiting_startUninterrupted(){
        testTooMuchWaiting(false);
    }

    public void testTooMuchWaiting(boolean startInterrupted){
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        long timeoutNs = millisToNanos(DELAY_SMALL_MS);
        AwaitNanosUninterruptiblyAndThrowThread t = scheduleAwaitNanosUninterruptiblyAndThrow(lock,condition,timeoutNs,startInterrupted);
        joinAll(t);

        t.assertIsTimedOut();
        t.assertIsTerminatedWithInterruptStatus(startInterrupted);
    }

    //===========================================================

    public void testInterruptedWhileWaiting_startInterrupted(){
        testInterruptedWhileWaiting(true);
    }

    public void testInterruptedWhileWaiting_startUninterrupted(){
        testInterruptedWhileWaiting(false);
    }

    public void testInterruptedWhileWaiting(boolean interrupted){
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        long timeoutNs = millisToNanos(DELAY_MEDIUM_MS);

        AwaitNanosUninterruptiblyAndThrowThread t = scheduleAwaitNanosUninterruptiblyAndThrow(lock,condition,timeoutNs,interrupted);

        sleepMs(DELAY_TINY_MS);
        t.assertIsStarted();

        t.interrupt();
        sleepMs(DELAY_TINY_MS);
        Thread signalThread = TestUtil.scheduleSignallAll(lock,condition);        
        joinAll(t,signalThread);
        t.assertIsTerminated();
        t.assertSuccess();
        t.assertIsTerminatedWithInterruptStatus();
    }
}
