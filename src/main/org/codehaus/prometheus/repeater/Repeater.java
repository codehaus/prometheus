/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A Repeater is a structure that keeps repeating a {@link Repeatable} task. If the task
 * is <tt>null</tt>, the Repeater does nothing.  You could see the Repeater as an engine, that
 * can turn an axle (the {@link Repeatable#execute()} method).
 * <p/>
 * <td><b>Remove threading from components</b></td>
 * <dd>
 * My personal experience is that it is better te remove threading from components because: it makes
 * components less complex, easier to test, better reusable (not always an issue) and better
 * customizable. Hooking up a Repeater from the outside to a component, is a great way to remove the
 * threading from components. It could be compared with externalizing the engine from a waterpump.
 * The waterpump exposes an axle (a method) and you can hook whatever engine (could be a Repeater)
 * you want to. For testing purposes you can turn this axle manually (by calling the method in your
 * unittest) and in a production environment you hook up a {@link ThreadPoolRepeater}.
 * <br/>
 * todo:
 * add example of code with a while(true) loop being refactored from internal thread that loops to
 * an external repeater.
 * </dd>
 * <p/>
 * <td><b>Using Repeater in Spring</b></td>
 * <dd>
 * I have gained a lot of new insights while working with the Spring Framework, and although Prometheus
 * doesn't depend on Spring, it can be used in Spring Projects (I have used it in various projects).
 * <td><b>Repeater vs Executor</b></td>
 * <dd>
 * The big difference between a Repeater and an Executor is that an Executor executes a task once and
 * the Repeater repeatedly executes the same task. Repeating the same task can be realized by modifying
 * the environment of the Executor, eg:
 * <ol>
 * <li>resubmit the task when it completed</li>
 * <li>modify a BlockingQueue so that it keeps handing out the same task over and over</li>
 * </ol>
 * But I found this behavior very unnatural and that is why I decided to create a new threadpool structure
 * that has the required behaviour.
 * </dd>
 *
 * todo:
 * I'm still not happy about the difference between a TimeoutException (or returning false) and
 * RejectedExecutionException.
 *
 * @author Peter Veentjer.
 * @see org.codehaus.prometheus.repeater.RepeaterService
 * @see org.codehaus.prometheus.repeater.ThreadPoolRepeater
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ThreadPoolExecutor
 */
public interface Repeater {

    /**
     * Starts repeating the given task at some point in the future. If the task can't be accepted
     * immediately (maybe because the repeater is strict about different task being executed
     * concurrently), the call can block.
     * <br/>
     * If the Repeateris currently running a task, this task is not interrupted.
     * <br/>
     * No guarantuee is made that the submitted task is executed. It could be replaced by a
     * different repeat call before it is executed before the first time.
     * <br/>
     * This call is responsive to interrupts, so if waiting is needed to place the task, this can
     * be interrupted. It is up to the implementation to decide if an InterruptedException is thrown.
     * <br/>
     *
     * @param task the task to repeat. This value can be null; if it is, the Repeater waits.
     * @throws java.util.concurrent.RejectedExecutionException
     *                              if the task can't be accepted.
     * @throws InterruptedException if the current thread is interrupted while waiting to place the
     *                              task into this Repeater.
     * @see #tryRepeat(Repeatable,long,TimeUnit)
     */
    void repeat(Repeatable task) throws InterruptedException;

    /**
     * Tries to submit this task for repeated execution, but without blocking. If the task can't be
     * accepted immeditately (maybe because the repeater is strict about different tasks being
     * executed concurrently) <tt>false</tt> is returned and otherwise <tt>true</tt>.
     * <p/>
     * If this Repeater currently is running a task, this task will not be interrupted.
     * <p/>
     * The interrupt status of the calling thread is ignored.
     * <p/>
     * The difference between returning false and throwing an RejectedExecutionException is that
     * false indicates that the submit could be retried later. A RejectedExecutionException indicates
     * that the message can't be executed because the Executor is not in the correct state (maybe it
     * already has shutdown).
     *
     * @param task the task to repeat. This value can be <tt>null</tt>.
     * @return <tt>true</tt> if the task has been accepted for repeating, <tt>false</tt> otherwise.
     * @throws java.util.concurrent.RejectedExecutionException
     *          if the task can't be accepted.
     */
    boolean tryRepeat(Repeatable task);

    /**
     * Tries to submit this task for repeated execution or block until it can be executed or until
     * a timeout occurs.
     * <p/>
     * If this Repeater currently is running a task, this task will not be interrupted.
     * <p/>
     * This call is responsive to interrupts, so it can be interrupted. It is up to the implementation
     * to decide if an InterruptedException is thrown.
     * <p/>
     * If the timeout is smaller than zero, a TimeoutException will be thrown.
     * <p/>
     *
     * @param task    the task to repeat. This value can be <tt>null</tt>.
     * @param timeout how long to wait before giving up
     * @param unit    a <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt>
     *                parameter.
     * @throws java.util.concurrent.RejectedExecutionException
     *                              if the task is not accepted.
     * @throws InterruptedException if the current thread is interrupted while waiting to place the
     *                              task into this Repeater.
     * @throws NullPointerException if unit is null.
     * @throws TimeoutException     if a timeout occurs while waiting to submit the task.
     * @see #repeat(Repeatable)
     */
    void tryRepeat(Repeatable task, long timeout, TimeUnit unit)
            throws InterruptedException, TimeoutException;
}
