/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import java.util.concurrent.*;

/**
 * The BlockingExecutor executes submitted tasks. The BlockingExecutor can be compared to the
 * {@link java.util.concurrent.Executor}, but the BlockingExecutor tryExecute more control on
 * timeouts and blocking behaviour.
 * <p/>
 * <td><b>What about Future's</b></td>
 * <dd>
 * The BlockingExecutor doesn't provide support for a {@link Future} because this can be realized by
 * using a {@link FutureTask}.
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
 * The BlockingExeutor doesn't provide support for executing a {@link java.util.concurrent.Callable}
 * because this also can be realized by using a FutureTask.
 * <pre>
 * Callable&lt;Integer&gt; task = new SomeCallable&lt;Integer&gt;();
 * FutureTask&lt;Integer&gt; futureTask = new FutureTask&lt;Integer&gt(task);
 * executor.execute(futureTask);
 * Integer result = futureTask.get();
 * </pre>
 * </dd>
 *
 * @author Peter Veentjer.
 */
public interface BlockingExecutor{

	/**
     * A difference with the {@link java.util.concurrent.Executor#execute(Runnable)} is that
     * this method blocks until the task can be executed, and the other method rejects the
     * task.
     *
     * Todo:
     * what about rejectionhandlers? Default no blocking rejectionhandler is
     * provided.
     *
	 * @param task the task to tryExecute.
	 * @throws InterruptedException if the current thread has been interrupted. If that happens,
	 *                              the task is not executed.
     * @throws RejectedExecutionException
	 * @throws NullPointerException if task is null.
	 */
	void execute(Runnable task) throws InterruptedException;

    /**
	 * Offers a task to this BlockingExecutor for (possible future) execution.
	 *
	 * @param task the task to tryExecute.
	 * @param timeout how long to wait before giving up, in units of unit
	 * @param unit    a TimeUnit determining how to interpret the timeout parameter
	 * @return true if successful, or false if the specified waiting time elapses before space is available.
	 * @throws InterruptedException if the current thread has been interrupted. If that happens,
	 *                              the task is not executed by this BlockingExecutor.
	 * @throws NullPointerException if task or unit is null.
     * @throws RejectedExecutionException
     * @throws TimeoutException 
	 */
	void tryExecute(Runnable task, long timeout, TimeUnit unit) throws InterruptedException,TimeoutException;
}
