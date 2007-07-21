/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.testsupport.TestUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author Peter Veentjer.
 */
public abstract class JucLatch_AbstractTest extends ConcurrentTestCase {

    public volatile JucLatch latch;

    public void newClosedLatch() {
        latch = new JucLatch(new ReentrantLock());
    }

    public void newOpenLatch() {
        latch = new JucLatch(new ReentrantLock());
        latch.open();
    }

    public void assertIsOpen() {
        assertTrue(latch.isOpen());
    }

    public void assertIsClosed() {
        assertFalse(latch.isOpen());
    }

    public Thread scheduleSpuriousWakeup() {
        return TestUtil.scheduleSpuriousWakeup(latch.getMainLock(), latch.getOpenCondition(), 0);
    }

    public void open() {
        //open the latch, and check that the awaitThread has finished successfully.
        OpenThread openThread = scheduleOpen();
        joinAll(openThread);
        openThread.assertIsTerminatedNormally();
        assertIsOpen();
    }

    public OpenThread scheduleOpen() {
        OpenThread t = new OpenThread();
        t.start();
        return t;
    }

    public OpenWithoutLockingThread scheduleOpenWithoutLocking(boolean startInterrupted) {
        OpenWithoutLockingThread t = new OpenWithoutLockingThread();
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public class OpenWithoutLockingThread extends TestThread {
        @Override
        public void runInternal() {
            latch.openWithoutLocking();
        }
    }

    public TimedTryAwaitThread scheduleTimedTryAwait(boolean startInterrupted, long timeoutMs) {
        TimedTryAwaitThread t = new TimedTryAwaitThread(timeoutMs);
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public AwaitThread scheduleAwait(boolean startInterrupted) {
        AwaitThread t = new AwaitThread();
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public class AwaitThread extends TestThread {
        @Override
        protected void runInternal() throws InterruptedException {
            latch.await();
        }
    }

    public class OpenThread extends TestThread {
        @Override
        public void runInternal() {
            latch.open();
        }
    }

    public class TimedTryAwaitThread extends TestThread {
        private volatile long timeoutMs;

        public TimedTryAwaitThread(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            latch.tryAwait(timeoutMs, TimeUnit.MILLISECONDS);
        }
    }


}
