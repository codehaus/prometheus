/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.uninterruptiblesection;

import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.util.Latch;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Unittests the {@link TimedUninterruptibleSection}.
 *
 * @author Peter Veentjer.
 */
public class TimedUninterruptibleSectionTest extends ConcurrentTestCase {

    private volatile TimedUninterruptibleSection section;
    private volatile Latch latch;

    public void setUp() throws Exception {
        super.setUp();
        latch = new Latch();
    }

    //========= testing arguments =================

    public void testArgs() throws TimeoutException {
        section = new TimedUninterruptibleSection() {
            protected Object originalsection(long timeoutNs) {
                fail();
                return null;
            }
        };

        try {
            section.tryExecute(1, null);
            fail();
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testNegativeTimeout() {
        section = new TimedUninterruptibleSection() {
            protected Object originalsection(long timeoutNs) {
                fail();
                return null;
            }
        };

        try {
            section.tryExecute(-1, TimeUnit.NANOSECONDS);
            fail();
        } catch (TimeoutException ex) {
            assertTrue(true);
        }
    }

    //========== no waiting is done ===============

    public void testNoWaitingIsDone_startUninterrupted() {
        testNoWaitingIsDone(START_UNINTERRUPTED);
    }

    public void testNoWaitingIsDone_startInterrupted() {
        testNoWaitingIsDone(START_INTERRUPTED);
    }

    public void testNoWaitingIsDone(boolean startInterrupted) {
        final Object returnValue = "foo";
        section = new TimedUninterruptibleSection() {
            protected Object originalsection(long timeoutNs) throws TimeoutException, InterruptedException {
                return returnValue;
            }
        };

        TimedExecuteThread executeThread = scheduleTimedExecute(startInterrupted, 0);
        joinAll(executeThread);
        executeThread.assertSuccess(returnValue);
        executeThread.assertIsTerminatedWithInterruptStatus(startInterrupted);
    }

    //========== some waiting is done ===========

    public void testSomeWaitingIsDone_startUninterrupted() {
        testSomeWaitingIsDone(START_UNINTERRUPTED);
    }


    public void testSomeWaitingIsDone_startInterrupted() {
        testSomeWaitingIsDone(START_UNINTERRUPTED);
    }

    public void testSomeWaitingIsDone(boolean startInterrupted) {
        final Object returnValue = 20;
        section = new TimedUninterruptibleSection() {
            protected Object originalsection(long timeoutNs) throws TimeoutException, InterruptedException {
                latch.tryAwait(timeoutNs, TimeUnit.NANOSECONDS);
                return returnValue;
            }
        };

        TimedExecuteThread executeThread = scheduleTimedExecute(startInterrupted, DELAY_LONG_MS);
        sleepMs(DELAY_SMALL_MS);
        executeThread.assertIsStarted();

        latch.open();
        joinAll(executeThread);
        executeThread.assertSuccess(returnValue);
        executeThread.assertIsTerminatedWithInterruptStatus(startInterrupted);
    }

    //========= interrupted while waiting ===========

    public void testInterruptedWhileWaiting_startUninterrupted() {
        testInterruptedWhileWaiting(START_UNINTERRUPTED);
    }

    public void testInterruptedWhileWaiting_startInterrupted() {
        testInterruptedWhileWaiting(START_INTERRUPTED);
    }

    public void testInterruptedWhileWaiting(boolean startInterrupted) {
        final Object returnValue = 10;
        section = new TimedUninterruptibleSection() {
            protected Object originalsection(long timeoutNs) throws TimeoutException, InterruptedException {
                latch.tryAwait(timeoutNs, TimeUnit.NANOSECONDS);
                return returnValue;
            }
        };

        TimedExecuteThread executeThread = scheduleTimedExecute(startInterrupted, DELAY_LONG_MS);
        sleepMs(DELAY_SMALL_MS);
        executeThread.assertIsStarted();

        executeThread.interrupt();
        sleepMs(DELAY_SMALL_MS);
        executeThread.assertIsStarted();

        latch.open();
        joinAll(executeThread);
        executeThread.assertSuccess(returnValue);
        executeThread.assertIsTerminatedWithInterruptStatus(true);
    }

    //======= timeout ================================

    public void testTimeout_startUninterrupted() {
        testTimeout(START_UNINTERRUPTED);
    }

    public void testTimeout_startInterrupted() {
        testTimeout(START_INTERRUPTED);
    }

    public void testTimeout(boolean startInterrupted) {
        section = new TimedUninterruptibleSection() {
            protected Object originalsection(long timeoutNs) throws TimeoutException, InterruptedException {
                latch.tryAwait(timeoutNs, TimeUnit.NANOSECONDS);
                return null;
            }
        };

        TimedExecuteThread executeThread = scheduleTimedExecute(startInterrupted, DELAY_SMALL_MS);
        joinAll(executeThread);
        executeThread.assertIsTimedOut();
        executeThread.assertIsTerminatedWithInterruptStatus(startInterrupted);
    }

    //========== runtime exception ============


    public void testRuntimeException_startUninterrupted() {
        testRuntimeException(START_UNINTERRUPTED);
    }

    public void testRuntimeException_startInterrupted() {
        testRuntimeException(START_UNINTERRUPTED);
    }

    public void testRuntimeException(boolean startInterrupted) {
        final RuntimeException ex = new RuntimeException();

        section = new TimedUninterruptibleSection() {
            protected Object originalsection(long timeoutNs) throws TimeoutException, InterruptedException {
                throw ex;
            }
        };

        TimedExecuteThread executeThread = scheduleTimedExecute(startInterrupted, DELAY_SMALL_MS);
        joinAll(executeThread);
        executeThread.assertIsTerminatedWithThrowing(ex.getClass());
        executeThread.assertIsTerminatedWithInterruptStatus(startInterrupted);
    }

    public TimedExecuteThread scheduleTimedExecute(boolean startInterrupted, long timeoutMs) {
        TimedExecuteThread t = new TimedExecuteThread(timeoutMs);
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    class TimedExecuteThread extends TestThread {
        private volatile Object foundReturnValue;
        private final long timeoutMs;


        public TimedExecuteThread(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        protected void runInternal() throws TimeoutException {
            foundReturnValue = section.tryExecute(timeoutMs, TimeUnit.MILLISECONDS);
        }

        public void assertSuccess(Object expectedReturnValue) {
            assertIsTerminated();
            assertSame(expectedReturnValue, foundReturnValue);
        }
    }
}
