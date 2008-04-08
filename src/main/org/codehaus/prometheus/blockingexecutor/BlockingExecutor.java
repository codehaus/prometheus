/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import java.util.concurrent.*;

/**
 * An Object that executes submitted tasks. The BlockingExecutor can be compared to the
 * {@link java.util.concurrent.Executor}, but provides more control on blocking behavior and timeouts.
 * <p/>
 * If a {@link ThreadPoolExecutor} receives a task, it places the task on the workqueue using the offer 
 * method and a zero timeout. If no place is available on that workqueue, the call doesn't block, but the
 * {@link java.util.concurrent.RejectedExecutionHandler} is called instead. The default handler throws a
 * {@link RejectedExecutionException}. In a lot of cases don't want to receive this exception, but want you
 * just want to block until space comes available, and you are left on your
 * own with the ThreadPoolExecutor. Another problem with the Executor is that you can't place a task with
 * a timeout per call (there is no easy way to pass the timeout parameter to the workqueue.offer).
 * <p/>
 * <td><b>What about Future's</b></td>
 * <dd>
 * The BlockingExecutor doesn't provide support for a {@link Future} directly, because this can be
 * realized by using a {@link FutureTask}. If the original task is wrapped inside a FutureTask and
 * this task executed, you will have your Future, example:
 * <pre>
 * Runnable task = new SomeRunnable();
 * FutureTask futureTask = new FutureTask(task,null);
 * executor.execute(futureTask);
 * futureTask.get();//wait for the completion
 * </pre>
 * </dd>
 * The same technique can be used to track a {@link java.util.concurrent.Callable} with a Future.
 * <p/>
 * <td>What about Callable's<b></b></td>
 * <dd>
 * The BlockingExeutor doesn't provide direct support for executing a {@link java.util.concurrent.Callable}
 * because this also can be realized by using a FutureTask.
 * <pre>
 * Callable&lt;Integer&gt; task = new SomeCallable&lt;Integer&gt;();
 * FutureTask&lt;Integer&gt; futureTask = new FutureTask&lt;Integer&gt(task);
 * executor.execute(futureTask);
 * Integer result = futureTask.get();
 * </pre>
 * </dd>
 * <p/>
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public interface BlockingExecutor {

    /**
     * Submits a task for execution. If there is no place to store the task, this call blocks until
     * one of the following things happens:
     * <ol>
     * <li>the task is accepted</li>
     * <li>the task is rejected and a RejectedExecutionException is thrown</li>
     * <li>the call is interrupted and an InterruptedException is thrown</li>
     * </ol>
     * <p/>
     * The main difference with this method and the {@link java.util.concurrent.Executor#execute(Runnable)} is that
     * the former blocks and the latter uses a {@link RejectedExecutionHandler} when the queue is full.
     *
     * @param task the task to execute.
     * @throws InterruptedException       if the current thread has been interrupted while waiting for task placement.
     *                                    If that happens, the task is not executed.
     * @throws RejectedExecutionException if the task is rejected for execution.
     * @throws NullPointerException       if task is <tt>null</tt>.
     */
    void execute(Runnable task) throws InterruptedException;

    /**
     * Offers a task for execution. If there is no place to store the task, this call block until
     * one of the following things happens:
     * <ol>
     * <li>the task is accepted</li>
     * <li>the task is rejected and a RejectedExecutionException is thrown</li>
     * <li>the call is interrupted and an InterruptedException is thrown</li>
     * <li>a timeout has occurred and a TimeoutException is thrown</li>
     * </ol>
     * The {@link Executor} doesn't allow for timeouts at the call level. An instance wide
     * timeout could be realized by creating a custom RejectedExecutionHandler and place the
     * item on the workqueue with a fixed timeout.
     *
     * @param Ttask    the task to execute.
     * @param timeout how long to wait before giving up, in units of unit
     * @param unit    a TimeUnit determining how to interpret the timeout parameter
     * @throws InterruptedException       if the current thread has been interrupted while waiting for task placement.
     *                                    If that happens the task is not executed.
     * @throws RejectedExecutionException if the task is rejected for execution.
     * @throws NullPointerException       if task is <tt>null</tt>.
     * @throws TimeoutException           if the call times out.
     */
    void tryExecute(Runnable task, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;
}
