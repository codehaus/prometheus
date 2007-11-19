/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.uninterruptiblesection;

import org.codehaus.prometheus.concurrenttesting.ConcurrentTestCase;
import org.codehaus.prometheus.concurrenttesting.TestThread;
import org.codehaus.prometheus.concurrenttesting.Delays;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.joinAll;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.sleepMs;
import org.codehaus.prometheus.util.JucLatch;
import org.codehaus.prometheus.util.Latch;

/**
 * Unittests the {@link UninterruptibleSection}.
 *
 * @author Peter Veentjer
 */
public class UninterruptibleSectionTest extends ConcurrentTestCase {
    private volatile UninterruptibleSection section;
    private volatile Latch latch;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        latch = new JucLatch();
    }

    //========= no blocking ==================

    public void testNoBlocking_startUninterrupted() {
        testNoBlocking(START_UNINTERRUPTED);
    }

    public void testNoBlocking_startInterrupted() {
        testNoBlocking(START_INTERRUPTED);
    }

    public void testNoBlocking(boolean startInterrupted) {
        final Object returnValue = 10;
        section = new UninterruptibleSection() {
            protected Object interruptibleSection() {
                return returnValue;
            }
        };

        ExecuteThread executeThread = scheduleExecute(startInterrupted);
        joinAll(executeThread);
        executeThread.assertSuccess(returnValue);
        executeThread.assertIsTerminatedWithInterruptFlag(startInterrupted);
    }

    //========= some blocking ==================

    public void testSomeWaitingNeeded_startUninterrupted() {
        testSomeWaitingNeeded(START_UNINTERRUPTED);
    }

    public void testSomeWaitingNeeded_startInterrupted() {
        testSomeWaitingNeeded(START_INTERRUPTED);
    }

    public void testSomeWaitingNeeded(boolean startInterrupted) {
        final Object returnValue = 20;
        section = new UninterruptibleSection() {
            protected Object interruptibleSection() throws InterruptedException {
                latch.await();
                return returnValue;
            }
        };

        ExecuteThread executeThread = scheduleExecute(startInterrupted);
        sleepMs(Delays.SMALL_MS);
        executeThread.assertIsStarted();

        latch.open();
        joinAll(executeThread);
        executeThread.assertSuccess(returnValue);
        executeThread.assertIsTerminatedWithInterruptFlag(startInterrupted);
    }

    //=========== interrupted while blocking ==============

    public void testInterruptedWhileWaiting_startUninterrupted() {
        testInterruptedWhileWaiting(START_UNINTERRUPTED);
    }

    public void testInterruptedWhileWaiting_startInterrupted() {
        testInterruptedWhileWaiting(START_INTERRUPTED);
    }

    public void testInterruptedWhileWaiting(boolean startInterrupted) {
        final Object returnValue = 30;
        section = new UninterruptibleSection() {
            protected Object interruptibleSection() throws InterruptedException {
                latch.await();
                return returnValue;
            }
        };

        ExecuteThread executeThread = scheduleExecute(startInterrupted);
        sleepMs(Delays.SMALL_MS);
        executeThread.assertIsStarted();

        executeThread.interrupt();
        sleepMs(Delays.SMALL_MS);
        executeThread.assertIsStarted();

        latch.open();
        joinAll(executeThread);
        executeThread.assertSuccess(returnValue);
        executeThread.assertIsTerminatedWithInterruptFlag(true);
    }

    //=========== runtime exception in block ================      

    public void testRuntimeException_startUninterrupted() {
        testRuntimeException(START_UNINTERRUPTED);
    }

    public void testRuntimeException_startInterrupted() {
        testRuntimeException(START_INTERRUPTED);
    }

    public void testRuntimeException(boolean startInterrupted) {
        final RuntimeException ex = new RuntimeException();
        section = new UninterruptibleSection() {
            protected Object interruptibleSection() {
                throw ex;
            }
        };

        ExecuteThread executeThread = scheduleExecute(startInterrupted);
        joinAll(executeThread);
        executeThread.assertIsTerminatedWithThrowing(ex.getClass());
        executeThread.assertIsTerminatedWithInterruptFlag(startInterrupted);
    }

    public ExecuteThread scheduleExecute(boolean startInterrupted) {
        ExecuteThread t = new ExecuteThread();
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    class ExecuteThread extends TestThread {
        private volatile Object foundReturnValue;

        @Override
        protected void runInternal() {
            foundReturnValue = section.execute();
        }

        public void assertSuccess(Object expectedReturnValue) {
            assertIsTerminatedNormally();
            assertSame(expectedReturnValue, foundReturnValue);
        }
    }
}
