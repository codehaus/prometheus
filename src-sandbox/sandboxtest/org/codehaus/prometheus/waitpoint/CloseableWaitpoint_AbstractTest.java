/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.waitpoint;

import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.testsupport.ConcurrentTestUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 */
public abstract class CloseableWaitpoint_AbstractTest extends ConcurrentTestCase {

    public volatile CloseableWaitpoint waitpoint;

    public void newClosedCloseableWaitpoint() {
        waitpoint = new CloseableWaitpoint(false);
    }

    public void newOpenCloseableWaitpoint() {
        waitpoint = new CloseableWaitpoint(true);
    }

    public void assertIsClosed() {
        assertIsOpen(false);
    }

    public void assertIsOpen() {
        assertIsOpen(true);
    }

    public void assertIsOpen(boolean open) {
        assertEquals(open, waitpoint.isOpen());
        assertEquals(!open, waitpoint.isClosed());
    }

    public TimedTryPassThread scheduleTimedTryPass(boolean startInterrupted, long timeoutMs) {
        TimedTryPassThread t = new TimedTryPassThread(timeoutMs, TimeUnit.MILLISECONDS);
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public TimedTryPassThread scheduleTimedTryPass(long timeoutMs) {
        TimedTryPassThread t = new TimedTryPassThread(timeoutMs, TimeUnit.MILLISECONDS);
        t.start();
        return t;
    }

    public PassThread schedulePass() {
        PassThread t = new PassThread();
        t.start();
        return t;
    }

    public PassThread schedulePass(boolean startInterrupted) {
        PassThread t = new PassThread();
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public OpenThread scheduleOpen(long delayMs) {
        OpenThread t = new OpenThread();
        t.setDelayMs(delayMs);
        t.start();
        return t;
    }

    public OpenThread scheduleOpen() {
        OpenThread t = new OpenThread();
        t.start();
        return t;
    }

    public OpenThread scheduleOpen(boolean startInterrupted) {
        OpenThread t = new OpenThread();
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }


    public TryPassThread scheduleTryPass(boolean startInterrupted) {
        TryPassThread t = new TryPassThread();
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public Thread scheduleSpuriousWakeup() {
        return ConcurrentTestUtil.scheduleSpuriousWakeup(waitpoint.getMainLock(), waitpoint.getOpenCondition(), 0);
    }

    public class TimedTryPassThread extends TestThread {
        private final long timeout;
        private final TimeUnit unit;
        private volatile long foundRemainingTimeoutNs;

        public TimedTryPassThread(long timeout, TimeUnit unit) {
            this.timeout = timeout;
            this.unit = unit;
        }

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            foundRemainingTimeoutNs = waitpoint.tryPass(timeout, unit);
        }

        public void assertSuccess(long maximumRemainingTimeoutMs) {
            assertIsTerminatedNormally();
            long ms = TimeUnit.NANOSECONDS.toMillis(foundRemainingTimeoutNs);
            String msg = String.format("maximum %d found %d", maximumRemainingTimeoutMs, foundRemainingTimeoutNs);
            assertTrue(
                    msg,
                    ms >= TimeUnit.NANOSECONDS.toMillis(foundRemainingTimeoutNs));
        }
    }

    public class PassThread extends TestThread {
        @Override
        protected void runInternal() throws InterruptedException {
            waitpoint.pass();
        }
    }

    public class OpenThread extends TestThread {
        @Override
        public void runInternal() throws Exception {
            waitpoint.open();
        }
    }

    public class TryPassThread extends TestThread {

        private Boolean success;

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            success = waitpoint.tryPass();
        }

        public void assertFailure() {
            assertIsTerminatedNormally();
            assertFalse(success);
        }

        public void assertSuccess() {
            assertIsTerminatedNormally();
            assertTrue(success);
        }
    }
}
