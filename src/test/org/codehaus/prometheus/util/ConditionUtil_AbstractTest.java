/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.testsupport.ConcurrentTestCase;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public abstract class ConditionUtil_AbstractTest extends ConcurrentTestCase {

    public AwaitNanosUninterruptiblyAndThrowThread scheduleAwaitNanosUninterruptiblyAndThrow(
            Lock lock, Condition condition, long timeoutNs, boolean interrupted) {
        AwaitNanosUninterruptiblyAndThrowThread t = new AwaitNanosUninterruptiblyAndThrowThread(lock, condition, timeoutNs);
        t.setStartInterrupted(interrupted);
        t.start();
        return t;
    }

    public AwaitAndThrowThread scheduleAwaitAndThrow(Lock lock, Condition condition, long timeoutMs) {
        AwaitAndThrowThread t = new AwaitAndThrowThread(lock, condition, timeoutMs, TimeUnit.MILLISECONDS);
        t.start();
        return t;
    }

    public long millisToNanos(long millis) {
        return TimeUnit.MILLISECONDS.toNanos(millis);
    }

    //todo: valt het een en ander te refactoren aan de herhalende logica in deze testthreads.
    public class AwaitAndThrowThread extends TestThread {
        private final Lock lock;
        private final Condition condition;
        private final long timeout;
        private final TimeUnit timeoutUnit;
        private volatile long foundRemainingTimeoutNs;


        public AwaitAndThrowThread(Lock lock, Condition condition, long timeout, TimeUnit timeoutUnit) {
            this.condition = condition;
            this.timeout = timeout;
            this.timeoutUnit = timeoutUnit;
            this.lock = lock;
        }

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            lock.lock();
            try {
                foundRemainingTimeoutNs = ConditionUtil.awaitAndThrow(condition, timeout, timeoutUnit);
            } finally {
                lock.unlock();
            }
        }


        public void assertIsSuccess(long expectedRemainingTimeoutNs) {
            assertIsTerminated();
            assertTrue(expectedRemainingTimeoutNs < foundRemainingTimeoutNs);
        }
    }

    public class AwaitNanosUninterruptiblyAndThrowThread extends TestThread {
        private final Lock lock;
        private final Condition condition;
        private final long timeoutNs;
        private volatile long remainingNs;

        public AwaitNanosUninterruptiblyAndThrowThread(Lock lock, Condition condition, long timeoutNs) {
            this.lock = lock;
            this.condition = condition;
            this.timeoutNs = timeoutNs;
        }

        @Override
        protected void runInternal() throws TimeoutException {
            lock.lock();
            try {
                remainingNs = ConditionUtil.awaitNanosUninterruptiblyAndThrow(condition, timeoutNs);
            } finally {
                lock.unlock();
            }
        }

        public void assertSuccess() {
            assertIsTerminated();
        }
    }
}
