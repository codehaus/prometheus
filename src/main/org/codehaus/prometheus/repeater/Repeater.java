/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A Repeater is a structure that keeps repeating a submitted task (a {@link Repeatable}. If the task
 * is <tt>null</tt>, the Repeater does nothing.
 * <p/>
 * <td><b>Remove threading from components</b></td>
 * <dd>
 * My personal experience is that it is better te remove threading from components because it makes
 * components a lot less complex, easier to test, better reusable (not always an issue) and better
 * customizable. Hooking up a Repeater from the outside to a component, is a great way to remove the
 * threading from components. It could be compared with externalizing the engine from a waterpump.
 * The waterpump exposes an axle (a method) and you can hook whatever engine (Repeater) you want to.
 * For testing purposes you can turn this axle manually (by calling the method in your unittest) and
 * in a production environment you hook up a {@link ThreadPoolRepeater}.
 * <br/>
 * todo:
 * add example of code with a while(true) loop being refactored from internal thread that loops to
 * an external repeater.
 * </dd>
 * <p/>
 * <td><b>Using Repeater in Spring</b></td>
 * <dd>
 * I have gained a lot of new insights while working with the Spring Framework, and although this
 * project doesn't depend on Spring, it can be used in Spring Projects (I have used it in various
 * projects). Example:
 * <pre>
 * todo: add spring configuration example
 * </pre>
 * </dd>
 * <p/>
 * <td><b>Repeater vs Executor</b></td>
 * <dd>
 * todo:the Repeater has methods with timoutsupport and allows blocking. executor executes a task
 * only once.
 * </dd>
 *
 * @author Peter Veentjer.
 * @see org.codehaus.prometheus.repeater.RepeaterService
 * @see org.codehaus.prometheus.repeater.ThreadPoolRepeater
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ThreadPoolExecutor
 */
public interface Repeater {

    /**
     * Starts repeating the given task at some point in the future. This call is responsive to
     * interrupts, so if waiting is needed to post the task, this can be interrupted. If the Repeater
     * is currently running a task, this task is not interrupted. In one of the following releases a
     * 'forceRepeat' will be added.
     * <p/>
     * No guarantuee is made that the submitted task is executed. It could be replaced by a
     * different repeat call before it is executed.
     * <p/>
     * It is up to the implementation to decide if an InterruptedException is thrown.
     *
     * @param task the task to repeat. This value can be null; it it is, this Repeater waits.
     * @throws java.util.concurrent.RejectedExecutionException
     *                              if the task is not accepted.
     * @throws InterruptedException if the current thread is interrupted while waiting to place the
     *                              task into this Repeater.
     * @see #tryRepeat(Repeatable,long,java.util.concurrent.TimeUnit)
     */
    void repeat(Repeatable task) throws InterruptedException;

    /**
     * Tries to submit this task for repeated execution. If the task can't be executed,
     * <tt>false</tt> is returned, otherwise <tt>true</tt>. Unlike the {@link #repeat(Repeatable)}
     * it doesn't throw a RejectedExecutionException. The interrupt status of the calling thread
     * is ignored. If this Repeater currently is running a task, this task will not be interrupted.
     * <p/>
     * todo: difference between rejectedexecutionexception and false.
     *
     * @param task the task to repeat. This value can be <tt>null</tt>.
     * @return <tt>true</tt> if the task has been accepted for repeating, <tt>false</tt> otherwise.
     * @throws java.util.concurrent.RejectedExecutionException
     *          if the task is not accepted.
     */
    boolean tryRepeat(Repeatable task);

    /**
     * Tries to submit this task for repeated execution. This call is responsive to interrupts, so
     * it can be interrupted. If this Repeater currently is running a task, this task will not be
     * interrupted.
     * <p/>
     * If the timeout is smaller than zero, a TimeoutException will be thrown.
     * <p/>
     * It is up to the implementation to decide if an InterruptedException is thrown.
     * <p/>
     * todo: difference between rejectedexecutionexception and false.
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
