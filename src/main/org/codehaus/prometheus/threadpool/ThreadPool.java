package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

/**
 * The ThreadPool is reponsible for managing a set of threads.
 *
 * <p/>
 * The workers in the ThreadPool keep repeating a {@link WorkerJob}. A threadpool can have
 * a default workjob for those cases you always want to keep repeating the same job over
 * and over. But a future improvement is planned: creating a Worker with a given WorkerJob.
 * For a worker to execute a WorkJob, it first needs to execute {@link WorkerJob#getWork()} method
 * and after it has got his task it calls the {@link org.codehaus.prometheus.threadpool.WorkerJob#runWork(Object)}
 * method. The getWork could be taking a reference from a LendableReference for example
 * (see {@link org.codehaus.prometheus.repeater.ThreadPoolRepeater}) or taking a task from a
 * blocking queue {@link org.codehaus.prometheus.blockingexecutor.ThreadPoolBlockingExecutor}. Workers
 * that are idle, are executing the getWork method (they are waiting for something to process) and else
 * they are working.
 * <p/>
 * <p/>
 * <p/>
 * Exception handler: by injecting an instanceof of an {@link ExceptionHandler} one is able to
 * handle exceptions. Default a {@link org.codehaus.prometheus.exceptionhandler.NullExceptionHandler} is used.
 * <p/>
 * <p/>
 * What does it mean when the poolsize grows.
 * does a worker need to repeat it's task? Or is this a responsibility from it's container like the repeater
 * or blockingexecutor.
 * <p/>
 * Threads in threadpools are reused, so you don't want to throw them away.
 * is it desirable to let different jobs in the threadpool?
 * <p/>
 * The threadpool should have a notice how much work has to be done. For every piece of work
 * this number is increased.
 * <p/>
 * Nothing is done with errors (they are not caught and this could lead to corrupted structures).
 *
 * @author Peter Veentjer.
 */
public interface ThreadPool {

    /**
     * Gets the job this ThreadPool is executing.  If no job is set, <tt>null</tt> is returned.
     *
     * @return the job this ThreadPool is executing.
     */
    WorkerJob getDefaultWorkerJob();

    /**
     * Sets the WorkerJob this ThreadPool executes.
     *
     * @param job the
     * @throws NullPointerException  if job is <tt>null</tt>.
     * @throws IllegalStateException if the ThreadPool isn't in the unstarted state anymore.
     */
    void setWorkerJob(WorkerJob job);

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
     * Starts this ThreadPool. If the ThreadPool already is started, the call is ignored.
     *
     * @throws IllegalStateException when the ThreadPool is shutting down, shutdown or
     *                               if threads need to be created, but no default WorkerJob is set.
     */
    void start();

    /**
     * Shuts down this ThreadPool. It doesn't interrupt workers while they are executing a task.
     * This call doesn't block while this ThreadPool is shutting down.
     *
     * @return the previous state the ThreadPool was in.
     */
    ThreadPoolState shutdown();

    /**
     * Shuts down this ThreadPool immediately. It does interrupt workers while they are executing
     * a task. This call doesn't block while this ThreadPool is shutting down.
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
