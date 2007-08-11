/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import static org.codehaus.prometheus.testsupport.ConcurrentTestUtil.giveOthersAChance;

/**
 * A Runnable that can be extended (to be used for testing purposes) and adds some
 * assertions methods. If this class is subclassed, the {@link #runInternal()} method
 * needs to be overridden if you want to execute logic to run.
 *
 * @author Peter Veentjer.
 */
///CLOVER:OFF
public class TestRunnable extends RunSupport implements Runnable {

    private final Runnable task;
    private final RuntimeException throwException;

    public TestRunnable() {
        task = null;
        throwException = null;
    }

    public TestRunnable(Runnable task) {
        if (task == null) throw new NullPointerException();
        this.task = task;
        this.throwException = null;
    }

    public TestRunnable(RuntimeException throwException){
        if(throwException == null)throw new NullPointerException();
        this.throwException = throwException;
        this.task = null;
    }

    /**
     * Returns the task this TestRunnable executed. If null is returned, no
     * task is executed.
     *
     * @return
     * @see #getThrowException()
     */
    public Runnable getTask() {
        return task;
    }

    /**
     * Returns the RuntimeException this TestRunnable throws. If null is returned,
     * no RuntimeException is thrown.
     *
     * @return
     */
    public RuntimeException getThrowException() {
        return throwException;
    }

    /**
     * Override this method if some logic needs to be executed.
     */
    public void runInternal() {
        if (task != null)
            task.run();
    }

    public final void run() {
        try {
            beginExecutionCount.incrementAndGet();
            actualRun();
        } finally {
            executedCount.incrementAndGet();
        }
    }

    private void actualRun() {
        if(throwException != null)
            throw throwException;

        if (catchruntimeexception) {
            try {
                runInternal();
            } catch (RuntimeException ex) {
                this.foundException = ex;
            }
        } else {
            runInternal();
        }
    }

    public void assertNotRunningAnymore() {
        int oldCount = getExecutedCount();
        giveOthersAChance(Delays.MEDIUM_MS);
        assertExecutedCount(oldCount);
    }
}
