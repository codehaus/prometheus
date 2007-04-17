/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import junit.framework.TestCase;
import org.codehaus.prometheus.util.ConcurrencyUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The CountingRunnable is a {@link Runnable} that keeps track of the
 * number of times it has been called. It doesn't do anything else.
 * <p/>
 * The CountingRunnable can be used in 3 different ways:
 * <ol>
 * <li>
 * subclass it and override the {@link #runInternal()} method.
 * </li>
 * <li>
 * inject a Runnable using the constructor. Now an arbitary runnable
 * can be decorator with counting functionality.
 * </li>
 * <li>
 * use an instance of the CountingRunnable as a runnable that keeps track
 * how much times it has been called.
 * </li>
 * </ol>
 * The count is always increased (even if the task throws a RuntimeException).
 * The count is increased after the task has executed.
 *
 * @author Peter Veentjer.
 */
public class CountingRunnable implements Runnable {

    private final AtomicLong count = new AtomicLong();
    private final Runnable task;

    public CountingRunnable(){
        task = null;
    }

    public CountingRunnable(Runnable task){
        if(task == null)throw new NullPointerException();
        this.task = task;
    }

    /**
     * Returns the number of times the run method is called. This value
     * could be stale at the moment it is received.
     */
    public long getCount() {
        return count.longValue();
    }

    /**
     * Returns the task that is injected. If no task is injected, null is returned.
     *
     * @return the injected task.
     */
    public Runnable getTask() {
        return task;
    }

    protected void runInternal() {
    }

    public final void run() {
        try {
            if(task!=null)
                task.run();
            else
                runInternal();
        } finally {
            count.incrementAndGet();
        }
    }

    /**
     * Asserts that the expected number of executions have occurred.
     *
     * @param expectedCount the expected count.
     * @throws IllegalArgumentException if expectedCount smaller than zero.
     */
    public void assertExecuteCount(long expectedCount) {
        if (expectedCount < 0) throw new IllegalArgumentException();
        TestCase.assertEquals(expectedCount, count.longValue());
    }

    public void assertNotExecuted() {
        TestCase.assertEquals(0, count.longValue());
    }

    public void assertExecutedMoreThanOnce() {
        TestCase.assertTrue(count.longValue() > 1);
    }

    public void assertExecutedOnceOrMore() {
        TestCase.assertTrue(count.longValue() > 0);
    }

    public void assertExecutedOnce() {
        TestCase.assertEquals(1, count.longValue());
    }

    //TODO: not very pretty, needs to be improved.
    public void assertNotRunningAnymore() {
        //make sure that the current task isn't running anymore.
        long oldCount = getCount();
        //todo: remove dependency.
        ConcurrencyUtil.sleepUninterruptibly(ConcurrentTestCase.DELAY_SMALL_MS, TimeUnit.MILLISECONDS);
        long newCount = getCount();
        TestCase.assertEquals(oldCount, newCount);
    }
}
