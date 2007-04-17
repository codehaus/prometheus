/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.testsupport.TestUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Peter Veentjer.
 */
public abstract class Latch_AbstractTest extends ConcurrentTestCase {

    public volatile Latch latch;

    public void newClosedLatch() {
        latch = new Latch(new ReentrantLock());
    }

    public void newOpenLatch() {
        latch = new Latch(new ReentrantLock());
        latch.open();
    }

    public void assertLatchOpen(){
        assertTrue(latch.isOpen());
    }

    public void assertLatchClosed(){
        assertFalse(latch.isOpen());
    }

    public Thread scheduleSpuriousWakeup() {
        return TestUtil.scheduleSpuriousWakeup(latch.getMainLock(), latch.getOpenCondition(), 0);
    }

    public OpenThread scheduleOpen() {
        OpenThread t = new OpenThread();
        t.start();
        return t;
    }

    public OpenWithoutLockingThread scheduleOpenWithoutLocking(boolean startInterrupted){
        OpenWithoutLockingThread t = new OpenWithoutLockingThread();
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public class OpenWithoutLockingThread extends TestThread {
        public void runInternal() {
            latch.openWithoutLocking();
        }
    }

    public TimedTryAwaitThread scheduleTimedTryAwait(boolean startInterrupted, long timeoutMs){
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

    public TryAwaitThread scheduleTryAwait(boolean startInterrupted){
        TryAwaitThread t = new TryAwaitThread();
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public class TryAwaitThread extends TestThread {
        private volatile boolean success;

        public void runInternal() {
            success = latch.tryAwait();
        }

        public void assertSuccess(){
            assertIsTerminatedWithoutThrowing();
            assertTrue(success);
        }

        public void assertFailure(){
            assertIsTerminatedWithoutThrowing();
            assertFalse(success);
        }
    }

    public class AwaitThread extends TestThread {
        protected void runInternal() throws InterruptedException {
            latch.await();
        }
    }

    public class OpenThread extends TestThread {
        public void runInternal() {
            latch.open();
        }
    }

    public class TimedTryAwaitThread extends TestThread {
        private volatile long timeoutMs;

        public TimedTryAwaitThread(long timeoutMs){
            this.timeoutMs = timeoutMs;
        }

        protected void runInternal() throws InterruptedException, TimeoutException {
            latch.tryAwait(timeoutMs, TimeUnit.MILLISECONDS);
        }
    }


}
