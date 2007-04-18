package org.codehaus.prometheus.threadpool;

/**
 * The Job a worker-thread in a ThreadPool executes. A WorkJob consists of 2 parts:
 * <ol>
 * <li>{@link #getTask()}: getting task to execute</li>
 * <li>{@link #executeTask(Object)}: executing the task itself</li>
 * </ol>
 * <p/>
 * Examples:
 * The getTask could be obtaining an item from a BlockingQueue (ThreadPoolBlockingExecutor} or
 * obtaining a reference from a LendableReference (ThreadPoolRepeater).
 * <p/>
 * <p/>
 * A WorkerJob can terminate the worker thread that executes. todo: needs more explanation.
 * <p/>
 * The reason that the task is seperated in 2 parts is that the getTask parts can be
 * interrupted if a threadpool needs to shutdown, or when an idle threads needs to be removed
 * because the threadpool is shrinking. 
 *
 * @author Peter Veentjer.
 */
public interface WorkerJob<E> {

    /**
     * Returns a piece of data required for execution.
     *
     * @return the data required for execution (is allowed to be null).
     * @throws InterruptedException if the calling thread was interrupted while waiting for a piece of data.
     */
    E getTask() throws InterruptedException;

    /**
     * Execute the task that was obtained by {@link #getTask()}.
     *
     * @param task the data required for execution (is allowed to be null).
     * @return true if the Worker should execute another time, false if it should terminate.
     * @throws Exception if something goes wrong while executing the task.
     */
    boolean executeTask(E task) throws Exception;
}
