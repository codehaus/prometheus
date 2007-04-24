package org.codehaus.prometheus.threadpool;

/**
 * The Job a worker-thread in a ThreadPool executes. A WorkJob consists of 2 parts:
 * <ol>
 * <li>{@link #getWork()}: getting task to execute</li>
 * <li>{@link #runWork(Object)}: executing the task itself</li>
 * </ol>
 * <p/>
 * Examples:
 * The getWork could be obtaining an item from a BlockingQueue (ThreadPoolBlockingExecutor} or
 * obtaining a reference from a LendableReference (ThreadPoolRepeater).
 * <p/>
 * <p/>
 * A WorkerJob can terminate the worker thread that executes. todo: needs more explanation.
 * <p/>
 * The reason that the task is seperated in 2 parts is that the getWork parts can be
 * interrupted if a threadpool needs to shutdown, or when an idle threads needs to be removed
 * because the threadpool is shrinking.
 * <p/>
 * I think this interface is so ThreadPool implementation specific, and that it should be removed
 * from the {@link ThreadPool} interface.
 *
 * The problem is with the getWork method. If a threadpool decides to shut down, it interrupts
 * all idle threads. If thread is blocking for work, the interrupted exception is caught, but the
 * threadpool decides to let the run the worker again -> deadlock. It wait for work that is
 * never comming. But if the threadpool decides to put the worker-thread down, it could lead
 * to unprocced work (the current situation at the BlockingThreadPoolExecutor).
 *
 * Could it happen that a threadpool shuts down, but a worker executes the getWork method. This
 * is a bad thing because that thread could be shut down 
 *
 * @author Peter Veentjer.
 */
public interface WorkerJob<E> {

    /**
     * Returns a piece of Work that needs to be run by {@link #runWork(Object)}. This call should
     * be responsive to interrupts because else Threadpool isn't able to interrupt idle threads.
     *
     * @return the data required for execution (value is not allowed to be null).
     * @throws InterruptedException if the calling thread was interrupted while waiting for a piece of data.
     */
    E getWork() throws InterruptedException;

    /**
     *
     * This call should not block indefinitely.
     *
     *
     * @return the retrieved work, null indicates that no work is available anymore for processing.
     * @throws InterruptedException
     */
    E getWorkWhileShuttingdown()throws InterruptedException;

    /**
     * Execute the work that was obtained by {@link #getWork()}.
     *
     * @param work the data required for execution. The value should never be null.
     * @return true if the Worker should execute another time, false if it should terminate.
     * @throws Exception if something goes wrong while executing the work.
     */
    void runWork(E work) throws Exception;
}
