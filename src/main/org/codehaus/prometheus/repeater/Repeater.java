/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An Object that keeps repeating a Repeater specific task: the {@link Repeatable}. If no Repeatable
 * reference is set, the Repeater does nothing.  You could see the Repeater as an engine, that can
 * turn an axle (the {@link Repeatable#execute()} method).  This seperation makes it easier to
 * create multithreaded structures, because the actual logic (the axle that is turned) is seperated
 * from the threading (the Repeater). Another advantage is that the Repeater is a reusable component
 * with Ideclearly defined behavior and well tested.
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
 * doesn't depend on Spring, it can be used in Spring projects (I have used it in various projects).
 * <td><b>Repeater vs Executor</b></td>
 * <dd>
 * The big difference between a Repeater and an {@link java.util.concurrent.Executor} is that an Executor
 * executes a task once and the Repeater repeatedly executes the same task. Repeating the same task can be
 * realized by modifying the environment of the Executor, eg:
 * <ol>
 * <li>resubmit the task when it completes</li>
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
 * @since 0.1
 */
public interface Repeater {

    /**
     * Starts repeating the given task at some point in the future. If the task can't be accepted
     * immediately (maybe because the repeater is strict about different task being executed
     * concurrently), the call is allowed to block.
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
     *                              if the task is rejected for execution.
     * @throws InterruptedException if the current thread is interrupted while waiting to submit the task.
     * @see #tryRepeat(Repeatable,long,TimeUnit)
     */
    void repeat(Repeatable task) throws InterruptedException;

    /**
     * Tries to submit this task for repeated execution, but without blocking. If the task can't be
     * accepted immeditately, maybe because the repeater is strict about different tasks being
     * executed concurrently, <tt>false</tt> is returned. If the task is accepted for execution,
     * true is returned.
     * <p/>
     * If this Repeater currently is running a task, this task will not be interrupted.
     * <p/>
     * The interrupt status of the calling thread is ignored.
     * <p/>
     * The difference between returning false and throwing an RejectedExecutionException is that
     * false indicates that a timeout has occurred. A RejectedExecutionException indicates
     * that the message can't be executed because the Executor is not in the correct state (maybe it
     * already has shutdown).
     *
     * todo:
     * there is a difference between this signature and the other try: one returns a boolean,
     * the other TimeoutException. This could lead to problems.
     *
     * @param task the task to repeat. This value can be <tt>null</tt>; meaning that the repeater
     *             won't be executing a task but 'pauzes'.
     * @return <tt>true</tt> if the task has been accepted for repeating, <tt>false</tt> otherwise.
     * @throws java.util.concurrent.RejectedExecutionException
     *          if the task is rejected for execution.
     */
    boolean tryRepeat(Repeatable task);

    /**
     * Tries to submit this task for repeated execution with a timeout. This call can complete in
     * 3 different ways:
     * <ol>
     * <li>the call completes and the task is executed in some point in the future</li>
     * <li>a InterruptedException is thrown to indicate that the call was interrupted</li>
     * <li>a TimeoutException is thrown to indicate that call has timed out</li>
     * <li>a RejectedExecutionException is thrown to indicate that this Repeater rejects the task
     * (because it is shutting down for example) </li>
     * </ol>
     *
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
     *                              if the task is rejected for execution.
     * @throws InterruptedException if the current thread is interrupted while waiting to submit the task
     * @throws NullPointerException if unit is null.
     * @throws TimeoutException     if a timeout occurs while waiting to submit the task.
     * @see #repeat(Repeatable)
     */
    void tryRepeat(Repeatable task, long timeout, TimeUnit unit)
            throws InterruptedException, TimeoutException;
}
