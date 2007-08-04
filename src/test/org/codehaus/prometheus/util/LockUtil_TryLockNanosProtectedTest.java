/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Unittest the {@link LockUtil#tryLockNanosProtected(Lock,long)} method.
 *
 * @author Peter Veentjer
 */
public class LockUtil_TryLockNanosProtectedTest extends LockUtil_AbstractTest {

    public void testArguments() throws InterruptedException, TimeoutException {
        try {
            LockUtil.tryLockNanosProtected(null, 1);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testFreeLockNoWaitingNeeded() {
        lock = new ReentrantLock();

        TryLockNanosProtectedThread tryLockThread = scheduleTryLockNanosProtected(0);
        joinAll(tryLockThread);
        tryLockThread.assertIsTerminatedNormally();
    }

    public void testSomeWaitingNeeded() throws InterruptedException {
        lock = new ReentrantLock();
        Thread lockAndUnlockThread = scheduleLockAndUnlock(DELAY_LONG_MS);
        giveOthersAChance();

        TryLockNanosProtectedThread tryLockThread = scheduleTryLockNanosProtected(DELAY_EON_MS);
        giveOthersAChance();
        tryLockThread.assertIsStarted();

        joinAll(lockAndUnlockThread, tryLockThread);
        tryLockThread.assertIsTerminatedNormally();
    }

    public void testTooMuchWaiting() throws InterruptedException {
        lock = new ReentrantLock();
        lockBySomeThread(DELAY_LONG_MS);

        TryLockNanosProtectedThread tryLockThread = scheduleTryLockNanosProtected(DELAY_SMALL_MS);
        joinAll(tryLockThread);
        tryLockThread.assertIsTimedOut();
    }

    public void testInterruptedWhileWaiting() {
        lock = new ReentrantLock();
        scheduleLockAndUnlock(DELAY_EON_MS);
        giveOthersAChance();

        TryLockNanosProtectedThread tryLockThread = scheduleTryLockNanosProtected(DELAY_EON_MS);
        giveOthersAChance();
        tryLockThread.assertIsStarted();

        tryLockThread.interrupt();
        joinAll(tryLockThread);
        tryLockThread.assertIsInterruptedByException();
    }
}
