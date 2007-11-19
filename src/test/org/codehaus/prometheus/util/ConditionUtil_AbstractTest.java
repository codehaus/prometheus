/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import org.codehaus.prometheus.concurrenttesting.ConcurrentTestCase;
import org.codehaus.prometheus.concurrenttesting.TestThread;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public abstract class ConditionUtil_AbstractTest extends ConcurrentTestCase {

    public AwaitNanosUninterruptiblyOrThrowThread scheduleAwaitNanosUninterruptiblyOrThrow(
            Lock lock, Condition condition, long timeoutNs, boolean interrupted) {
        AwaitNanosUninterruptiblyOrThrowThread t = new AwaitNanosUninterruptiblyOrThrowThread(lock, condition, timeoutNs);
        t.setStartInterrupted(interrupted);
        t.start();
        return t;
    }

    public AwaitOrThrowThread scheduleAwaitOrThrow(Lock lock, Condition condition, long timeoutMs) {
        AwaitOrThrowThread t = new AwaitOrThrowThread(lock, condition, timeoutMs, TimeUnit.MILLISECONDS);
        t.start();
        return t;
    }

    public long millisToNanos(long millis) {
        return TimeUnit.MILLISECONDS.toNanos(millis);
    }

    //todo: valt het een en ander te refactoren aan de herhalende logica in deze testthreads.
    public class AwaitOrThrowThread extends TestThread {
        private final Lock lock;
        private final Condition condition;
        private final long timeout;
        private final TimeUnit timeoutUnit;
        private volatile long foundRemainingTimeoutNs;


        public AwaitOrThrowThread(Lock lock, Condition condition, long timeout, TimeUnit timeoutUnit) {
            this.condition = condition;
            this.timeout = timeout;
            this.timeoutUnit = timeoutUnit;
            this.lock = lock;
        }

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            lock.lock();
            try {
                foundRemainingTimeoutNs = ConditionUtil.awaitOrThrow(condition, timeout, timeoutUnit);
            } finally {
                lock.unlock();
            }
        }


        public void assertIsSuccess(long expectedRemainingTimeoutNs) {
            assertIsTerminatedNormally();
            assertTrue(expectedRemainingTimeoutNs < foundRemainingTimeoutNs);
        }
    }

    public class AwaitNanosUninterruptiblyOrThrowThread extends TestThread {
        private final Lock lock;
        private final Condition condition;
        private final long timeoutNs;
        private volatile long remainingNs;

        public AwaitNanosUninterruptiblyOrThrowThread(Lock lock, Condition condition, long timeoutNs) {
            this.lock = lock;
            this.condition = condition;
            this.timeoutNs = timeoutNs;
        }

        @Override
        protected void runInternal() throws TimeoutException {
            lock.lock();
            try {
                remainingNs = ConditionUtil.awaitNanosUninterruptiblyOrThrow(condition, timeoutNs);
            } finally {
                lock.unlock();
            }
        }

        public void assertSuccess() {
            assertIsTerminatedNormally();
        }
    }
}
