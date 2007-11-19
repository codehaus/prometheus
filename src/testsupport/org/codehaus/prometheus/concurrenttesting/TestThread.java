/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.concurrenttesting;

import static junit.framework.TestCase.*;
import org.codehaus.prometheus.util.ConcurrencyUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Idea: at the moment only the testcase-executing thread is allowed to do asserts. Maybe allow
 * different threads also to make asserts.
 * <p/>
 * The TestThread doesn't use the {@link Thread#getState()} as a mechanism to determine the
 * state this Thread is in, see 12.1.2 of "Java Concurrency in Practice" for more information.
 * <p/>
 * The states a TestThread can be in are:
 * <ol>
 * <li><b>new</b>: when the thread has been created but hasn't been running</li>
 * <li><b>running</b>: a thread that is running, but hasn't been terminated. A thread that is blocking
 * also is in the running state.
 * </li>
 * <li>terminated: when the thread completed the execution of his task</li>
 * </ol>
 *
 * @author Peter Veentjer.
 */
public class TestThread extends Thread {

    protected volatile long delay = 0;
    protected volatile TimeUnit delayUnit;
    protected volatile Throwable foundThrowable;
    protected volatile boolean startInterrupted = false;
    protected volatile boolean isInterruptedAtEnd = false;
    protected volatile Stopwatch stopwatch = new Stopwatch();
    protected volatile ThreadState state = ThreadState.NEW;

    private final Runnable task;

    /**
     * Creates a TestThread with a 0 delay.
     */
    public TestThread() {
        task = this;
    }

    public TestThread(Runnable task) {
        if (task == null) throw new NullPointerException();
        this.task = task;
    }

    public final Stopwatch getStopwatch() {
        return stopwatch;
    }

    public final long getDelay() {
        return delay;
    }

    /**
     * Returns the delay timeunit. If no delay is given, the return value is undefined.
     *
     * @return the timeunit of delay.
     */
    public final TimeUnit getDelayUnit() {
        return delayUnit;
    }

    /**
     * Sets the interrupt status of the thread when it starts.
     *
     * @param startInterrupted true if the thread should running, interrupted, false otherwise.
     * @throws IllegalStateException if the TestThread isn't in the java.lang.Thread.State.NEW state anymore.
     */
    public void setStartInterrupted(boolean startInterrupted) {
        ensureNew();
        this.startInterrupted = startInterrupted;
    }

    /**
     * Ensures that the TestThread is in the Thread.State.NEW state. If it isn't an
     * IllegalStateException is thrown.
     */
    protected void ensureNew() {
        if (state != ThreadState.NEW)
            throw new IllegalStateException();
    }

    /**
     * Returns the thrown Exception. If no Exception is thrown by this TestThread,
     * null is returned. This method can be called anytime.
     *
     * @return the thrown RuntimeException.
     */
    public final Throwable getFoundThrowable() {
        return foundThrowable;
    }

    /**
     * Returns true if this TestThread should start interrupted, false otherwise.
     * This method can be called anytime.
     *
     * @return true if this Thread should start interrupted, false otherwise.
     */
    public final boolean isStartInterrupted() {
        return startInterrupted;
    }

    /**
     * Make sure that the TestThread is in the Thread.State.TERMINATED state.
     *
     * @throws IllegalStateException if the TestThread isn't terminated.
     */
    private void ensureTerminated() {
        if (state != ThreadState.TERMINATED)
            throw new IllegalStateException();
    }


    /**
     * Sets the delay.
     *
     * @param delay
     * @param delayUnit
     * @throws NullPointerException  if delayUnit is null.
     * @throws IllegalStateException if the TestThread isn't in the java.lang.Thread.State.NEW state anymore.
     */
    public final void setDelay(long delay, TimeUnit delayUnit) {
        if (delayUnit == null) throw new NullPointerException();
        ensureNew();
        this.delay = delay;
        this.delayUnit = delayUnit;
    }

    /**
     * Sets the delay in milliseconds.
     *
     * @param delayMs the delay in milliseconds.
     * @throws IllegalStateException if the TestThread isn't in the java.lang.Thread.State.NEW state anymore.
     */
    public final void setDelayMs(long delayMs) {
        setDelay(delayMs, TimeUnit.MILLISECONDS);
    }

    protected void runInternal() throws Exception {
        if (task == null)
            throw new IllegalStateException("runInternal method should be overridden, or a task injected");
        task.run();
    }

    public final void run() {
        state = ThreadState.STARTED;
        stopwatch.start();
        try {
            //todo: remove dependency
            if (delay > 0)
                ConcurrencyUtil.sleepUninterruptibly(delay, delayUnit);

            if (startInterrupted)
                interrupt();

            runInternal();
        } catch (Throwable ex) {
            this.foundThrowable = ex;
        } finally {
            stopwatch.stop();
            isInterruptedAtEnd = isInterrupted();
            state = ThreadState.TERMINATED;
        }
    }

    /**
     * Asserts that the TestThread is terminated (exited, finished). No check is done
     * if a RuntimeException is thrown.
     */
    public final void assertIsTerminated() {
        assertEquals(ThreadState.TERMINATED, state);
    }

    /**
     * Returns true if the TestThread had the interrupt status when it terminated. False
     * otherwise. If the TestThread is in the Thread.State.TERMINATED state, a
     * IllegalStateException is thrown.
     *
     * @return true if the TestThread had the interrupt status when it terminated.
     * @throws IllegalStateException if the TestThread is not terminated.
     */
    public boolean isTerminatedWithInterruptedFlag() {
        ensureTerminated();
        return isInterruptedAtEnd;
    }


    /**
     * Asserts that the TestThread is in the new state (so hasn't started running).
     */
    public final void assertIsNew() {
        assertEquals(ThreadState.NEW, state);
    }

    /**
     * Asserts that the TestThread is running (this also includes blocking).
     */
    public final void assertIsStarted() {
        assertEquals(ThreadState.STARTED, state);
    }

    /**
     * Asserts that the TestThread is terminated and no Throwable is thrown.
     */
    public final void assertIsTerminatedNormally() {
        assertIsTerminated();
        if (foundThrowable != null)
            foundThrowable.printStackTrace();
        assertNull(foundThrowable);
    }

    /**
     * Asserts that the TestThread is terminated and a Throwable is thrown.
     *
     * @param throwableClass the class of the Throwable
     * @throws NullPointerException if throwableClass is null.
     */
    public final void assertIsTerminatedWithThrowing(Class throwableClass) {
        if (throwableClass == null) throw new NullPointerException();

        assertIsTerminated();
        assertNotNull("no exception found", foundThrowable);
        assertTrue(format("exception.class %s is not a subclass of %s", throwableClass, foundThrowable.getClass()),
                throwableClass.isInstance(foundThrowable));
        printInterrestingStacktrace();
    }

    /**
     * Prints the stacktrace of the found throwable. If the throwable is in instanceof InterruptedException
     * or TimeoutException, no trace is printed because these exceptions are very likely to occur (they are
     * part of the logic). If there is no found throwable, nothing happens.
     */
    public final void printInterrestingStacktrace() {
        if (foundThrowable == null)
            return;

        boolean uninterresting = foundThrowable instanceof InterruptedException ||
                foundThrowable instanceof TimeoutException;
        if (uninterresting)
            return;

        foundThrowable.printStackTrace();
    }

    /**
     * Asserts that the TestThread is terminated and a Exception is thrown. Check
     * is done on reference.
     *
     * @param expectedThrowable the expected Exception
     * @throws NullPointerException if expectedThrowable is null.
     */
    public final void assertIsTerminatedWithThrowing(Throwable expectedThrowable) {
        if (expectedThrowable == null) throw new NullPointerException();

        assertIsTerminated();

        assertNotNull(foundThrowable);
        assertSame(expectedThrowable, foundThrowable);
        printInterrestingStacktrace();
    }


    /**
     * Asserts that the TestThread has not encountered any runtime exceptions.
     * No assumption is made on the state the thread is in, so this call can be
     * called anytime.
     */
    public final void assertNoRuntimeException() {
        assertNull(foundThrowable);
    }

    /**
     * Interrupts the Thread and joins. If the thread can't be joined in a
     * certain amount of time, the call fails.
     */
    public final void interruptAndJoin() {
        interrupt();
        try {
            join(Delays.LONG_MS);
            if (isAlive())
                fail(String.format("thread %s is still alive",this));
        } catch (InterruptedException e) {
            fail("interrupted while waiting to join");
        }
    }

    /**
     * Checks if the TestThread has the interrupted status set
     * at the end of execution. No check is done on exception.
     * <p/>
     * The {@link Thread#isInterrupted()} can't be used, because
     * it is only useable when the thread is in a running state,
     * and throws false when the thread completes (no matter what
     * the value was). That is why the isInterrupted is stored when
     * the thread completes and can be used for later use.
     *
     * @param interrupted true if the interrupt flag should be set, false otherwise.
     */
    public final void assertIsTerminatedWithInterruptFlag(boolean interrupted) {
        assertIsTerminated();
        assertEquals(interrupted, isInterruptedAtEnd);
    }

    /**
     * Checks if the current Thread has terminated (no check is done on Exception)
     * and that the interrupt status was set.
     * <p/>
     * This call doesn't clear the interrupted status.
     */
    public final void assertIsTerminatedWithInterruptFlag() {
        assertIsTerminatedWithInterruptFlag(true);
    }

    /**
     * Asserts that the TestThread#runInternal method threw an InterruptedException.
     */
    public final void assertIsTerminatedByInterruptedException() {
        assertIsTerminatedWithThrowing(InterruptedException.class);
    }

    /**
     * Asserts that the TestThread#runInternal method threw a TimeoutException.
     */
    public final void assertIsTimedOut() {
        assertIsTerminatedWithThrowing(TimeoutException.class);
    }
}
