package org.codehaus.prometheus.threadpool;

/**
 * The Job a worker-thread in a ThreadPool executes. A WorkJob consists of 2 parts:
 * <ol>
 * <li>{@link #getWork()}: getting task to execute</li>
 * <li>{@link #executeWork(Object)}: executing the task itself</li>
 * </ol>
 * <p/>
 * Examples:
 * The getWork could be obtaining an item from a BlockingQueue (ThreadPoolBlockingExecutor} or
 * obtaining a reference from a LendableReference (ThreadPoolRepeater).
 * <p/>
 * <p/>
 * A ThreadPoolJob can terminate the worker thread that executes by returning false in the
 * executeWork method (behavior while shutting down is undefined at the moment).
 * <p/>
 * The reason that the task is seperated in 2 parts is that the getWork parts can be
 * interrupted if a threadpool needs to shutdown, or when idle threads needs to be removed
 * because the threadpool is shrinking.
 * <p/>
 * I think this interface is so StandardThreadPool implementation specific, and that it should
 * be removed from the {@link ThreadPool} interface.
 * <p/>
 * The problem is with the getWork method. If a threadpool decides to shut down, it interrupts
 * all idle threads. If thread is blocking for work, the interrupted exception is caught, but the
 * threadpool decides to let the run the worker again -> deadlock. It wait for work that is
 * never comming. But if the threadpool decides to put the worker-thread down, it could lead
 * to unprocced work (the current situation at the BlockingThreadPoolExecutor). That is the reason
 * why the getWorkWhileShuttingDown was added. As soon as a Worker-thread is interrupted, it will
 * never use the getWork method anymore, but uses the getShuttingdownWork method instead. This
 * call should not block for ever.
 *
 * @author Peter Veentjer.
 */
public interface ThreadPoolJob<E> {

    /**
     * Returns a piece of Work that needs to be run by {@link #executeWork(Object)}. This call should
     * be responsive to interrupts because else the Threadpool isn't able to interrupt idle threads.
     * This is important because else it isn't able to force a shutdown.
     *
     * @return the data required for execution (value is not allowed to be null).
     * @throws InterruptedException if the calling thread was interrupted while waiting for work
     */
    E getWork() throws InterruptedException;

    /**
     * This call should not block indefinitely.
     * 
     * @return the retrieved work, null indicates that no work is available anymore for processing.
     * @throws InterruptedException if the thread is interrupted while getting the shuttingdown work.
     */
    E getShuttingdownWork() throws InterruptedException;

    /**
     * Execute the work that was obtained by {@link #getWork()}.
     *
     * @param work the data required for execution. The value should never be null.
     * @throws Exception if something goes wrong while executing the work.
     * @return true if the job should be executed again, false otherwise.
     */
    boolean executeWork(E work) throws Exception;
}
