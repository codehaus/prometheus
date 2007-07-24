package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

/**
 * <p>
 * The ThreadPool is reponsible for managing a set of threads. Threads in threadpools are reused, so you don't
 * want to throw them away.
 * </p>
 * <p>
 * The workers in the ThreadPool keep repeating a {@link ThreadPoolJob}. A threadpool can have
 * a default workjob for those cases you always want to keep repeating the same job over
 * and over. But a future improvement is planned: creating a Worker with a given ThreadPoolJob.
 * For a worker to execute a WorkJob, it first needs to execute {@link ThreadPoolJob#getWork()} method
 * and after it has got his task it calls the {@link ThreadPoolJob#executeWork(Object)}
 * method. The getWork could be taking a reference from a LendableReference for example
 * (see {@link org.codehaus.prometheus.repeater.ThreadPoolRepeater}) or taking a task from a
 * blocking queue {@link org.codehaus.prometheus.blockingexecutor.ThreadPoolBlockingExecutor}. Workers
 * that are idle, are executing the getWork method (they are waiting for work to execute) and else
 * they are working.
 * </p>
 * <p>
 * When a ThreadPool is shutdown, it starts calling the {@link ThreadPoolJob#getShuttingdownWork()}.
 * When a ThreadPool is forced to shutdown by calling the {@link #shutdownNow()} method, no methods
 * are called on the ThreadPoolJob anymore and the pooled thread is able to rest in peace.
 * </p>
 * <h1>Exception handling</h1>
 * <p>
 * Exception handler: by injecting an instanceof of an {@link ExceptionHandler} one is able to
 * handle exceptions. Default a {@link org.codehaus.prometheus.exceptionhandler.NullExceptionHandler}
 * is used.
 * <p/>
 * <p/>
 * Nothing is done with errors (they are not caught and this could lead to corrupted structures).
 * <p/>
 *
 * @author Peter Veentjer.
 */
public interface ThreadPool {

    /**
     * Gets the job this ThreadPool is executing.  If no job is set, <tt>null</tt> is returned.
     *
     * @return the job this ThreadPool is executing.
     */
    ThreadPoolJob getWorkerJob();

    /**
     * Sets the ThreadPoolJob this ThreadPool executes.
     *
     * @param job the
     * @throws NullPointerException  if job is <tt>null</tt>.
     * @throws IllegalStateException if the ThreadPool isn't in the unstarted state anymore.
     */
    void setWorkerJob(ThreadPoolJob job);

    /**
     * Gets the ExceptionHandler. The value will never be <tt>null</tt>.
     *
     * @return the ExceptionHandler of this ThreadPool.
     */
    ExceptionHandler getExceptionHandler();

    /**
     * Sets the ExceptionHandler of this ThreadPool.
     *
     * @param handler the new ExceptionHandler
     * @throws NullPointerException if handler is <tt>null</tt>.
     */
    void setExceptionHandler(ExceptionHandler handler);

    /**
     * @return
     */
    Lock getStateChangeLock();

    /**
     * Returns the state this ThreadPool is in.
     *
     * @return the state this ThreadPool is in.
     */
    ThreadPoolState getState();

    /**
     * Starts this ThreadPool. If the ThreadPool already is running, the call is ignored.
     *
     * @throws IllegalStateException when the ThreadPool is shutting down, shutdown or
     *                               if threads need to be created, but no default ThreadPoolJob is set.
     */
    void start();

    /**
     * Shuts down this ThreadPool. It doesn't interrupt workers while they are executing a task.
     * This call doesn't wait for the shutdown to complete.
     *
     * @return the previous state the ThreadPool was in.
     */
    ThreadPoolState shutdown();

    /**
     * Shuts down this ThreadPool immediately. It does interrupt workers while they are executing
     * a task. This call doesn't wait for the shutdown to complete.
     *
     * @return the previous state the ThreadPool was in.
     */
    ThreadPoolState shutdownNow();

    /**
     * Awaits the shutdown of the ThreadPool but doesn't influence the state of the ThreadPool. This
     * call can be made safely no matter the state the ThreadPool is in. If the ThreadPool already is
     * shutdown, it returned immediately.
     *
     * @throws InterruptedException if the thread is interrupted while waiting for the complete shutdown
     *                              to take place.
     */
    void awaitShutdown() throws InterruptedException;

    /**
     * Awaits the shutdown of the ThreadPool but doesn't influence the state of the ThreadPool. This
     * call can be made safely no matter the state the ThreadPool is in. If the ThreadPool already is
     * shutdown, it returned immediately.
     *
     * @param timeout how long to wait before giving up, in units of unit
     * @param unit    a TimeUnit determining how to interpret the timeout parameter
     * @throws InterruptedException if the thread is interrupted while waiting for the complete shutdown
     *                              to take place.
     * @throws TimeoutException     if a timeout occurred.
     * @throws NullPointerException if unit is <tt>null</tt>.
     */
    void tryAwaitShutdown(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;

    /**
     * Returns the current number of threads in the pool.
     *
     * @return the current number.
     */
    int getActualPoolSize();

    /**
     * Gets the desired poolsize. The desired poolsize doesn't have to match the actual poolsize.
     * It can take some time for the pool to grow or shrink to the desired poolsize.
     *
     * @return the desired poolsize.
     */
    int getDesiredPoolSize();

    /**
     * Sets the desired poolsize of this ThreadPool. The actual poolsize doesn't have to match
     * the desired poolsize because growing and shrinking of the pool can take some time. When
     * the threadpool already is shutting down, or shutdown, it is illegal to call this method.
     *
     * @param desiredPoolsize the desired size of the threadpool.
     * @throws IllegalArgumentException if desiredPoolsize is smaller than 0.
     * @throws IllegalStateException    if the ThreadPool is shutting down, or shutdown.
     */
    void setDesiredPoolsize(int desiredPoolsize);
}
