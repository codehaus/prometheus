package org.codehaus.prometheus.util;

import org.easymock.EasyMock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Unittests the {@link LockUtil#tryLockNanos(Lock, long)} method.
 *
 * @author Peter Veentjer.
 */
public class LockUtil_TryLockNanosTest extends LockUtil_AbstractTest {

    public void testArguments() throws InterruptedException {
        try {
            LockUtil.tryLockNanos(null, 1);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testNoWaitingNeeded() throws InterruptedException {
        lock = new ReentrantLock();

        TryLockThread tryLockThread = scheduleTryLock(0, START_UNINTERRUPTED);
        joinAll(tryLockThread);

        tryLockThread.assertSuccess();        
        assertLockUnavailable();
    }

    public void testNegativeTimeout() throws InterruptedException {
        lock = new ReentrantLock();

        long remainingNs = LockUtil.tryLockNanos(lock, -1);
        assertTrue(remainingNs < 0);
        assertLockAvailable();
    }

    public void testSomeWaitingNeeded() throws InterruptedException {
        newLockedLock(DELAY_MEDIUM_MS);

        TryLockThread tryLock = scheduleTryLock(DELAY_LONG_MS,START_UNINTERRUPTED);

        sleepMs(DELAY_TINY_MS);
        tryLock.assertIsStarted();

        joinAll(tryLock);
        tryLock.assertSuccess();
    }

    public void testTooMuchWaiting() throws InterruptedException {
        newLockedLock(DELAY_LONG_MS);
        long remaining = LockUtil.tryLockNanos(lock, 100);
        assertTrue(remaining < 0);        
    }

    public void testInterruptedWhileWaiting() throws InterruptedException {
        long timeoutNs = 100;
        lock = EasyMock.createMock(Lock.class);
        EasyMock.expect(lock.tryLock(timeoutNs, TimeUnit.NANOSECONDS)).andThrow(new InterruptedException());
        EasyMock.replay(lock);

        try {
            LockUtil.tryLockNanos(lock, timeoutNs);
            fail("InterruptedException expected");
        } catch (InterruptedException ex) {
            assertTrue(true);
        }
    }

}
