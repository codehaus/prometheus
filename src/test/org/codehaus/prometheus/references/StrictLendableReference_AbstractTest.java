/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import org.codehaus.prometheus.testsupport.ConcurrentTestCase;
import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.joinAll;
import org.codehaus.prometheus.testsupport.TestThread;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Peter Veentjer.
 */
public abstract class StrictLendableReference_AbstractTest<E> extends ConcurrentTestCase {

    public StrictLendableReference<E> lendableRef;

    public void assertPutWaits(E ref) {
        PutThread<E> putter = schedulePut(ref);
        giveOthersAChance();
        putter.assertIsStarted();
    }

    public void spawned_put(E ref) {
        E oldRef = lendableRef.peek();

        PutThread<E> t = schedulePut(ref);
        joinAll(t);
        t.assertSuccess(oldRef);
    }

    public void assertLendCount(int expectedLendCount) {
        assertEquals(expectedLendCount, lendableRef.getLendCount());
    }

    public void assertHasRef(E expectedRef) {
        assertSame(expectedRef, lendableRef.peek());
    }

    public PutThread spawned_pendingPut(E newRef) {
        PutThread t = schedulePut(newRef);
        giveOthersAChance();
        t.assertIsStarted();
        return t;
    }

    public void spawned_takeback(E ref) {
        TakeBackThread<E> t = scheduleTakeback(ref);
        joinAll(t);
        t.assertSuccess();
    }

    public void spawned_put(E newRef, E expectedOldRef) {
        PutThread<E> t = schedulePut(newRef);
        joinAll(t);
        t.assertSuccess(expectedOldRef);
        assertHasRef(newRef);
    }

    public void spawned_takebackAndReset(E ref) {
        TakebackAndResetThread<E> t = scheduleTakebackAndReset(ref);
        joinAll(t);
        t.assertIsTerminatedNormally();
    }

    public void spawned_take(E expectedTakenRef) {
        TakeThread<E> t = scheduleTake();
        joinAll(t);
        t.assertSuccess(expectedTakenRef);
    }

    public TakeThread<E> scheduleTake() {
        return scheduleTake(START_UNINTERRUPTED);
    }

    public TakeThread<E> scheduleTake(boolean startInterrupted) {
        TakeThread<E> t = new TakeThread<E>(lendableRef);
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public TakeBackThread<E> scheduleTakeback(E ref) {
        TakeBackThread<E> thread = new TakeBackThread<E>(lendableRef, ref);
        thread.start();
        return thread;
    }

    public LendThread<E> scheduleLend(E takebackRef, long lendPeriodMs) {
        LendThread<E> t = new LendThread<E>(
                lendableRef, takebackRef, lendPeriodMs, TimeUnit.MILLISECONDS);
        t.start();
        return t;
    }

    public PutThread<E> schedulePut(E ref) {
        PutThread<E> t = new PutThread<E>(lendableRef, ref);
        t.start();
        return t;
    }

    public PutThread<E> schedulePut(E ref, boolean startInterrupted) {
        PutThread<E> t = new PutThread<E>(lendableRef, ref);
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public MultipleTakebackThread scheduleMultipleTakebacks(int count, E ref) {
        MultipleTakebackThread t = new MultipleTakebackThread(count, ref);
        t.start();
        return t;
    }

    public TimedTryTakeThread scheduleTimedTryTake(long timeoutMs) {
        TimedTryTakeThread t = new TimedTryTakeThread(timeoutMs);
        t.start();
        return t;
    }

    public TakebackAndResetThread<E> scheduleTakebackAndReset(E ref) {
        TakebackAndResetThread<E> t = new TakebackAndResetThread<E>(lendableRef, ref);
        t.start();
        return t;
    }

    public SpuriousWakeupsThread scheduleSpuriousWakeups() {
        SpuriousWakeupsThread t = new SpuriousWakeupsThread();
        t.start();
        return t;
    }

    public TimedTryPutThread<E> scheduleTimedTryPut(E ref, long timeoutMs) {
        TimedTryPutThread<E> t = new TimedTryPutThread<E>(lendableRef, ref, timeoutMs);
        t.start();
        return t;
    }

    public TryTakeThread<E> scheduleTryTake(boolean startInterrupted) {
        TryTakeThread<E> t = new TryTakeThread<E>(lendableRef);
        t.setStartInterrupted(startInterrupted);
        t.start();
        return t;
    }

    public class SpuriousWakeupsThread extends TestThread {

        @Override
        public void runInternal() {
            lendableRef.getMainLock().lock();
            try {
                lendableRef.getNoTakersCondition().signalAll();
                lendableRef.getRefAvailableCondition().signalAll();
            } finally {
                lendableRef.getMainLock().unlock();
            }
        }
    }

    public class TimedTryTakeThread extends TestThread {
        private final long timeout;
        private volatile E foundTakenRef;

        public TimedTryTakeThread(long timeoutMs) {
            this.timeout = timeoutMs;
        }

        @Override
        protected void runInternal() throws InterruptedException, TimeoutException {
            foundTakenRef = lendableRef.tryTake(timeout, TimeUnit.MILLISECONDS);
        }

        public void assertSuccess(E expectedTakeRef) {
            assertIsTerminatedNormally();
            assertSame(expectedTakeRef, foundTakenRef);
        }
    }

    public class MultipleTakebackThread extends TestThread {
        private final int count;
        private final E ref;
        private boolean completed = false;

        public MultipleTakebackThread(int count, E ref) {
            this.count = count;
            this.ref = ref;
        }

        @Override
        public void runInternal() {
            for (int k = 0; k < count; k++)
                lendableRef.takeback(ref);
            completed = true;
        }

        public void assertSuccess() {
            assertIsTerminatedNormally();
            assertTrue(completed);
        }
    }
}
