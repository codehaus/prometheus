package org.codehaus.prometheus.util;

import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.testsupport.ConcurrentTestCase;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class LockUtil_AbstractTest extends ConcurrentTestCase {

    protected volatile Lock lock;

    public void newLockedLock(long durationMs) throws InterruptedException {
        lock = new ReentrantLock();
        lockBySomeThread(durationMs);
    }

    public Thread lockBySomeThread(final long durationMs) throws InterruptedException {
        Runnable r = new Runnable() {
            public void run() {
                lock.lock();
                System.out.println("locked");
                try{
                    sleepMs(durationMs);
                }finally{
                    System.out.println("unlocked");
                    lock.unlock();
                }
            }
        };

        Thread t = new Thread(r);
        t.start();

        //give other thread time to run
        Thread.yield();
        return t;
    }

    public void assertLockAvailable() throws InterruptedException {
        TestThread t = new TestThread() {
            public void runInternal() throws InterruptedException, TimeoutException {
                lock.tryLock(10, TimeUnit.MILLISECONDS);
            }
        };

        t.start();
        t.join();
        t.assertIsTerminated();
    }

    public void assertLockUnavailable() throws InterruptedException {
        TestThread t = new TestThread() {
            public void runInternal() throws InterruptedException {
                lock.lockInterruptibly();
            }
        };

        t.start();
        sleepMs(DELAY_SMALL_MS);
        t.assertIsStarted();
    }


    public TryLockThread scheduleTryLock(long timeoutMs, boolean startInterrupted) {
        TryLockThread t = new TryLockThread(timeoutMs);
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public class TryLockThread extends TestThread {
        private final long timeoutMs;
        private volatile long foundTimeoutNs;

        public TryLockThread(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        protected void runInternal() throws InterruptedException, TimeoutException {
            foundTimeoutNs = LockUtil.tryLockNanos(lock, TimeUnit.MILLISECONDS.toNanos(timeoutMs));
        }

        public void assertSuccess() {
            assertIsTerminated();
        }
    }


    public TryLockNanosProtectedThread scheduleTryLockNanosProtected(long timeoutMs) {
        TryLockNanosProtectedThread t = new TryLockNanosProtectedThread(timeoutMs);
        t.start();
        return t;
    }

    public ScheduleLockAndUnlockThread scheduleLockAndUnlock(long duration) {
        ScheduleLockAndUnlockThread t = new ScheduleLockAndUnlockThread(duration);
        t.start();
        return t;
    }


    public class ScheduleLockAndUnlockThread extends TestThread {
        private final long durationMs;

        public ScheduleLockAndUnlockThread(long durationMs) {
            this.durationMs = durationMs;
        }

        protected void runInternal() throws InterruptedException, TimeoutException {
            lock.lock();
            try {
                sleepMs(durationMs);
            } finally {
                lock.unlock();
            }
        }
    }

    public class TryLockNanosProtectedThread extends TestThread {
        private final long timeoutMs;

        public TryLockNanosProtectedThread(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        protected void runInternal() throws InterruptedException, TimeoutException {
            LockUtil.tryLockNanosProtected(lock, TimeUnit.MILLISECONDS.toNanos(timeoutMs));
        }
    }
}
