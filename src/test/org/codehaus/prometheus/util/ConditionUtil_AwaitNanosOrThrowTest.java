/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import static org.easymock.EasyMock.*;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionUtil_AwaitNanosOrThrowTest extends ConditionUtil_AbstractTest {

    private Condition conditionMock;

    public void setUp() throws Exception {
        super.setUp();
        conditionMock = createMock(Condition.class);
    }

    public void testArguments() throws InterruptedException, TimeoutException {
        try {
            ConditionUtil.awaitNanosOrThrow(null, 10);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testInterruptedWhileWaiting() throws InterruptedException, TimeoutException {
        long timeout = 10;
        expect(conditionMock.awaitNanos(timeout)).andThrow(new InterruptedException());
        replay(conditionMock);

        try {
            ConditionUtil.awaitNanosOrThrow(conditionMock, timeout);
            fail("InterruptedException expected");
        } catch (InterruptedException e) {
            assertTrue(true);
        }
        verify(conditionMock);
    }

    public void testIllegalMonitorState() throws TimeoutException, InterruptedException {
        Lock lock = new ReentrantLock();
        Condition cond = lock.newCondition();

        long timeouts = TimeUnit.SECONDS.toNanos(1);
        try {
            ConditionUtil.awaitNanosOrThrow(cond, timeouts);
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

        reset(conditionMock);
        expect(conditionMock.awaitNanos(timeout)).andReturn(remainingTimeoutNs);
        replay(conditionMock);

        try {
            ConditionUtil.awaitNanosOrThrow(conditionMock, timeout);
            assertTrue(!timeoutOccurred);
        } catch (TimeoutException e) {
            assertTrue(timeoutOccurred);
        }
        verify(conditionMock);
    }
}
