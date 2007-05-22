/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.testsupport.ConcurrentTestCase;

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

    public void assertPutSucceeds(E ref) {
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

    public PutThread tested_pendingPut(E newRef) {
        PutThread putThread = schedulePut(newRef);
        giveOthersAChance();
        putThread.assertIsStarted();
        return putThread;
    }

    public void tested_takeback(E ref) {
        TakeBackThread<E> t = scheduleTakeback(ref);
        joinAll(t);
        t.assertSuccess();
    }

    public void tested_put(E newRef, E expectedOldRef) {
        PutThread<E> putThread = schedulePut(newRef);
        joinAll(putThread);
        putThread.assertSuccess(expectedOldRef);
        assertHasRef(newRef);
    }

    public void tested_takebackAndReset(E ref) {
        TakebackAndResetThread<E> t = scheduleTakebackAndReset(ref);
        joinAll(t);
        t.assertIsTerminatedNormally();
    }

    public E tested_take(E expectedTakenRef) {
        TakeThread<E> takeThread = scheduleTake();
        joinAll(takeThread);
        takeThread.assertSuccess(expectedTakenRef);
        assertHasRef(expectedTakenRef);
        return takeThread.getTakenRef();
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
