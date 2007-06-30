/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import org.easymock.EasyMock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionUtil_AwaitNanosAndThrowTest extends ConditionUtil_AbstractTest {

    private Condition conditionMock;

    public void setUp() throws Exception {
        super.setUp();
        conditionMock = EasyMock.createMock(Condition.class);
    }

    public void testArguments() throws InterruptedException, TimeoutException {
        try {
            ConditionUtil.awaitNanosAndThrow(null, 10);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testInterruptedWhileWaiting() throws InterruptedException, TimeoutException {
        long timeout = 10;
        EasyMock.expect(conditionMock.awaitNanos(timeout)).andThrow(new InterruptedException());
        EasyMock.replay(conditionMock);

        try {
            ConditionUtil.awaitNanosAndThrow(conditionMock, timeout);
            fail("InterruptedException expected");
        } catch (InterruptedException e) {
            assertTrue(true);
        }
        EasyMock.verify(conditionMock);
    }

    /*
    public void testALittleWaiting() throws TimeoutException, InterruptedException {
        Lock lock = new ReentrantLock();
        Condition cond = lock.newCondition();

        TestThread t = new TestThread(){

            protected void runInternal() throws InterruptedException, TimeoutException {
                ConditionUtil.awaitAndThrow(cond,)
            }
        };
        t.start();

        


        scheduleSignallAll(lock, cond, 500);
        lock.lock();

        long timeoutNs = TimeUnit.SECONDS.toNanos(1);
        long remainingNs = ConditionUtil.awaitNanosAndThrow(cond, timeoutNs);
        assertTrue(remainingNs >= 0);
    }*/


    public void testIllegalMonitorState() throws TimeoutException, InterruptedException {
        Lock lock = new ReentrantLock();
        Condition cond = lock.newCondition();

        long timeouts = TimeUnit.SECONDS.toNanos(1);
        try {
            ConditionUtil.awaitNanosAndThrow(cond, timeouts);
            fail("IllegalMonitorStateException expected");
        } catch (IllegalMonitorStateException e) {
            assertTrue(true);
        }
    }

    public void testNegativeTimeout() throws InterruptedException {
        testTimeoutOccurred(-1, true);
    }

    public void testTooMuchWaiting() throws InterruptedException {
        testTimeoutOccurred(1, false);
        testTimeoutOccurred(0, true);
    }

    public void testTimeoutOccurred(long remainingTimeoutNs, boolean timeoutOccurred) throws InterruptedException {
        long timeout = 10;

        EasyMock.reset(conditionMock);
        EasyMock.expect(conditionMock.awaitNanos(timeout)).andReturn(remainingTimeoutNs);
        EasyMock.replay(conditionMock);

        try {
            ConditionUtil.awaitNanosAndThrow(conditionMock, timeout);
            assertTrue(!timeoutOccurred);
        } catch (TimeoutException e) {
            assertTrue(timeoutOccurred);
        }
        EasyMock.verify(conditionMock);
    }
}
