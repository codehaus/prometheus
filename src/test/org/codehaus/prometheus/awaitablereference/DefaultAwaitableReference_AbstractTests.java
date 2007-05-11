/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.awaitablereference;

import junit.framework.TestCase;
import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.testsupport.TestUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The DefaultAwaitableReferenceTests is a utility class that contains waitpoint methods
 * for testing the {@link AwaitableReference}.
 *
 * @author Peter Veentjer.
 */
public abstract class DefaultAwaitableReference_AbstractTests extends ConcurrentTestCase {

    public volatile DefaultAwaitableReference<Integer> awaitableRef;

    public void assertHasReference(Integer ref) {
        assertSame(ref, awaitableRef.peek());
    }

    public TimedTryPutThread scheduleTryPut(Integer ref, long timeoutMs) {
        TimedTryPutThread t = new TimedTryPutThread(ref, timeoutMs, TimeUnit.MILLISECONDS);
        t.start();
        return t;
    }

    public Thread scheduleSpuriousWakeup(long waitMs) {
        return TestUtil.scheduleSpuriousWakeup(
                awaitableRef.getMainLock(),
                awaitableRef.getReferenceAvailableCondition(),
                waitMs);
    }

    public PutThread schedulePut(Integer ref, boolean startInterrupted) {
        PutThread t = new PutThread(ref);
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public Thread scheduleSpuriousWakeup() {
        return scheduleSpuriousWakeup(0);
    }

    public TakeThread scheduleTake() {
        TakeThread taker = new TakeThread();
        taker.start();
        return taker;
    }

    public TakeThread scheduleTake(boolean startInterrupted) {
        TakeThread t = new TakeThread();
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public TimedTryTakeThread scheduleTryTake(long timeoutMs) {
        TimedTryTakeThread taker = new TimedTryTakeThread(timeoutMs, TimeUnit.MILLISECONDS);
        taker.start();
        return taker;
    }

    public TimedTryTakeThread scheduleTryTake(long timeoutMs, boolean startInterrupted) {
        TimedTryTakeThread taker = new TimedTryTakeThread(timeoutMs, TimeUnit.MILLISECONDS);
        taker.setStartInterrupted(startInterrupted);
        taker.start();
        return taker;
    }

    public PutThread schedulePut(Integer newRef) {
        PutThread t = new PutThread(newRef);
        t.start();
        return t;
    }

    public TryTakeThread scheduleTryTake() {
        TryTakeThread t = new TryTakeThread();
        t.start();
        return t;
    }

    public TryTakeThread scheduleTryTake(boolean startInterrupted) {
        TryTakeThread t = new TryTakeThread();
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public void put(Integer newRef, Integer expectedReturnedRef) {
        PutThread putter = schedulePut(newRef);
        joinAll(putter);
        putter.assertSuccess(expectedReturnedRef);
    }

    public void take(Integer expectedTakenRef) {
        TakeThread taker = scheduleTake();
        joinAll(taker);
        taker.assertSuccess(expectedTakenRef);
    }

    public class PutThread extends TestThread {

        private final Integer newRef;
        private volatile Integer foundOldRef;

        public PutThread(Integer newRef) {
            this.newRef = newRef;            
        }

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            foundOldRef = awaitableRef.put(newRef);
        }

        public void assertSuccess(Integer expectedOldRef) {
            assertIsTerminatedWithoutThrowing();
            TestCase.assertEquals(expectedOldRef, foundOldRef);
        }
    }

    public class TakeThread extends TestThread {

        private volatile Integer foundTakenRef;

        @Override
        protected void runInternal() throws InterruptedException {
            foundTakenRef = awaitableRef.take();
        }

        public void assertSuccess(Integer expectedTakenRef) {
            assertIsTerminatedWithoutThrowing();
            TestCase.assertSame(expectedTakenRef, foundTakenRef);
        }
    }

    public class TimedTryPutThread extends TestThread {

        private final long timeout;
        private final TimeUnit timeoutUnit;
        private final Integer ref;
        private volatile Integer foundRef;

        public TimedTryPutThread(Integer ref, long timeout, TimeUnit timeoutUnit) {
            this.timeout = timeout;
            this.timeoutUnit = timeoutUnit;
            this.ref = ref;
        }

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            foundRef = awaitableRef.tryPut(ref, timeout, timeoutUnit);
        }

        public void assertSuccess(Integer expectedReplacement) {
            assertIsTerminatedWithoutThrowing();
            assertSame(expectedReplacement, foundRef);
        }
    }

    public class TimedTryTakeThread extends TestThread {

        private final long timeout;
        private final TimeUnit unit;
        private volatile Integer foundTakenRef;

        public TimedTryTakeThread(long timeout, TimeUnit unit) {
            this.timeout = timeout;
            this.unit = unit;
        }

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            foundTakenRef = awaitableRef.tryTake(timeout, unit);
        }

        public void assertSuccess(Integer expectedTakenRef) {
            assertIsTerminatedWithoutThrowing();
            TestCase.assertSame(expectedTakenRef, foundTakenRef);
        }
    }

    public class TryTakeThread extends TestThread {
        private volatile Integer foundRef;

        @Override
        public void runInternal() {
            foundRef = awaitableRef.tryTake();
        }

        public void assertSuccess(Integer expectedRef) {
            assertIsTerminatedWithoutThrowing();
            TestCase.assertSame(expectedRef, foundRef);
        }
    }


}
