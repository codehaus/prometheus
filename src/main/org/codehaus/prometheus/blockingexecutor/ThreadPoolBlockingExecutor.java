/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.threadpool.StandardThreadPool;
import org.codehaus.prometheus.threadpool.ThreadPool;
import org.codehaus.prometheus.threadpool.ThreadPoolJob;
import org.codehaus.prometheus.threadpool.ThreadPoolState;
import static org.codehaus.prometheus.util.ConcurrencyUtil.ensureNoTimeout;

import static java.lang.String.format;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * An implementation of a {@link BlockingExecutorService} that uses a {@link ThreadPool} for thread
 * management and a workqueue (a {@link BlockingQueue}) to store unprocessed jobs.
 * <p/>
 * <h1>Out of order execution</h1>
 * Event though a blocking queue is used to store the items,and an item won't be returned out of order
 * (FIFO contract) it could be that:
 * <ol>
 * <li>a task takes a longer time than others to execute</li>
 * <li>a taken task hasn't had the time to execute (maybe unlucky context switches).</li>
 * </ol>
 * The FIFO contract can also be broken, check the {@link PriorityBlockingQueue}. Only when there is a
 * single thread you get the FIFO guarantee.
 * <p/>
 * <h1>Bounded workqueue</h1>
 * <p/>
 * If you don't have control on the number of tasks in the workqueue, this could lead to resource problems
 * like running out of memory. That is why it is better to use a bounded BlockingQueue (an unbounded
 * blockingqueue would also not lead to blocking behaviour).
 * </p>
 * <h1>Workqueue without internal capacity</h1>
 * <p/>
 * In some cases you don't want any unprocessed work, if that is the case, you can use a
 * {@link SynchronousQueue} as workqueue. A SynchronousQueue only accepts tasks if there is a
 * worker thread waiting for it. If no worker thread is available, the submission of the task blocks.
 * </p>
 * If a task placement is running before a shutdown, but completes after the system is shutting down,
 * the placing thread is responsible to make sure that the task is processed. This is done by removing
 * the task from the queue if it is still there and throwing a RejectedExecutionException or if the
 * task isn't on the queue, it is (being) processed.
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public class ThreadPoolBlockingExecutor implements BlockingExecutorService {

    public static ThreadPool createDefaultThreadPool(ThreadFactory threadFactory, int poolsize) {
        return new StandardThreadPool(poolsize, threadFactory);
    }

    public static BlockingQueue<Runnable> createDefaultWorkQueue() {
        return new LinkedBlockingQueue<Runnable>();
    }

    public static ThreadPool createDefaultThreadPool(int poolsize) {
        return new StandardThreadPool(poolsize);
    }

    private final ThreadPool threadPool;
    private final BlockingQueue<Runnable> workQueue;

    /**
     * Creates a ThreadPoolBlockingExecutor with the given number of threads.
     *
     * @param poolsize the initial number of threads in the ThreadPool.
     * @throws IllegalArgumentException if poolsize smaller than 0.
     */
    public ThreadPoolBlockingExecutor(int poolsize) {
        this(createDefaultThreadPool(poolsize), createDefaultWorkQueue());
    }

    /**
     * Creates a ThreadPoolBlockingExecutor with the given poolsize, ThreadPool
     * and workqueue.
     *
     * @param poolsize  the initial number of threads in the threadpool
     * @param factory   the ThreadFactory responsible for filling the threadpool
     * @param workQueue the BlockingQueue used to store unprocessed work.
     * @throws IllegalArgumentException if poolsize is smaller than zero.
     * @throws NullPointerException     if factory or workQueue is null.
     */
    public ThreadPoolBlockingExecutor(int poolsize, ThreadFactory factory, BlockingQueue<Runnable> workQueue) {
        this(createDefaultThreadPool(factory, poolsize), workQueue);
    }

    /**
     * Creates a ThreadPoolBlockingExecutor with the given ThreadPool and workqueue.
     *
     * @param threadPool the ThreadPool that is used to manage threads.
     * @param workQueue  the BlockingQueue used to store unprocessed work.
     * @throws NullPointerException if threadPool or workQueue is null.
     */
    public ThreadPoolBlockingExecutor(ThreadPool threadPool, BlockingQueue<Runnable> workQueue) {
        if (threadPool == null || workQueue == null) throw new NullPointerException();
        this.threadPool = threadPool;
        this.workQueue = workQueue;
        this.threadPool.setJob(new ThreadPoolJobImpl());
    }

    /**
     * Returns the ThreadPool this ThreadPoolBlockingExecutor uses to manage threads.
     *
     * @return the ThreadPool this ThreadPoolBlockingExecutor uses to manage threads.
     */
    public ThreadPool getThreadPool() {
        return threadPool;
    }

    /**
     * Returns the workqueue this ThreadPoolBlockingExecutor uses to store unprocesed
     * work. This queue should not directly be used to placed work because it might not
     * get processed (because the it isn't running anymore).
     *
     * @return the workqueue this ThreadPoolBlockingExecutor uses to store unprocessed work.
     */
    public BlockingQueue<Runnable> getWorkQueue() {
        return workQueue;
    }

    public void start() {
        threadPool.start();
    }

    /**
     * Returns the actual number of threads in the ThreadPool.
     *
     * @return the actual number of threads in the ThreadPool.
     */
    public int getActualPoolSize() {
        return threadPool.getActualPoolSize();
    }

    /**
     * Returns the desired number of threads in the ThreadPool.
     *
     * @return the desired number of threads in the ThreadPool.
     */
    public int getDesiredPoolSize() {
        return threadPool.getDesiredPoolSize();
    }

    /**
     * Sets the desired number of threads in the ThreadPool.
     *
     * @param poolSize the desired number of threads in the ThreadPool.
     * @throws IllegalArgumentException if poolsize smaller than 0.
     * @throws IllegalStateException    if the ThreadPool already is shutting down, or is shut down.
     */
    public void setDesiredPoolSize(int poolSize) {
        threadPool.setDesiredPoolsize(poolSize);
    }

    public ExceptionHandler getExceptionHandler() {
        return threadPool.getExceptionHandler();
    }

    public void setExceptionHandler(ExceptionHandler handler) {
        threadPool.setExceptionHandler(handler);
    }

    public void shutdown() {
        //todo
        //if the threadpool has no threads, it can't process work in the workqueue. So this
        //needs to be tackled in the interface of BlockingExecutorService
        threadPool.shutdown();
    }

    public List<Runnable> shutdownNow() {
        threadPool.shutdownNow();
        return drainWorkQueue();
    }

    /**
     * Drains all the items from the workqueue.
     * <p/>
     * Drains all items from the workqueue. Execute-threads that are pending for placement, are not
     * required to have placed their item before this call being called. If this is the case,
     * their item will be placed on the workqueue. These execute-threads are responsible themselfes
     * for ensuring that the task is placed. See {@link #ensureTaskHandeled(Runnable)} for more
     * information.
     *
     * @return a List containing all items in the workqueue (in the same order as on the workqueue).
     */
    private List<Runnable> drainWorkQueue() {
        List<Runnable> runnables = new ArrayList<Runnable>();
        workQueue.drainTo(runnables);
        return runnables;
    }

    public BlockingExecutorServiceState getState() {
        ThreadPoolState state = threadPool.getState();
        switch (state) {
            case unstarted:
                return BlockingExecutorServiceState.Unstarted;
            case running:
                return BlockingExecutorServiceState.Running;
            case forcedshuttingdown://fall through                      
            case shuttingdown:
                return BlockingExecutorServiceState.Shuttingdown;
            case shutdown:
                return BlockingExecutorServiceState.Shutdown;
            default:
                throw new RuntimeException(format("unhandeled threadpoolstate %s", state));
        }
    }

    public void awaitShutdown() throws InterruptedException {
        threadPool.awaitShutdown();
    }

    public void tryAwaitShutdown(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        threadPool.tryAwaitShutdown(timeout, unit);
    }

    public void execute(Runnable task) throws InterruptedException {
        if (task == null) throw new NullPointerException();

        ensurePoolRunning();
        workQueue.put(task);
        ensureTaskHandeled(task);
    }

    public void tryExecute(Runnable task, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (task == null || unit == null) throw new NullPointerException();

        ensureNoTimeout(timeout);
        ensurePoolRunning();
        if (!workQueue.offer(task, timeout, unit))
            throw new TimeoutException();
        ensureTaskHandeled(task);
    }

    private void ensurePoolRunning() {
        if (threadPool.getState() != ThreadPoolState.running)
            throw new RejectedExecutionException("task can't be executed, the ThreadPoolBlockingExecutor" +
                    " isn't running");
    }

    /**
     * Ensures that a task is handled. If a task is placed, a check is done if the threadpool
     * stil is running. After that the item is placed on the queue. If the queue is full, the
     * call block untill space gets available. The problem is that the threadpoolexecutor can
     * shutdown and that the placed task will never be executed. This methods makes sure that
     * the task is handled:
     * <ol>
     * <li>because it is executed</li>
     * <li>because it is rejected by throwing a RejectedExecutionException</li>
     * </ol>
     * <p/>
     * Logic inside this method has been inspired by the ThreadPoolExecutor.
     *
     * @param task the task to ensure to be handled.
     */
    private void ensureTaskHandeled(Runnable task) {
        //the structure is still running, the calling thread isn't responsible anymore
        //for the handling of the task.
        if (threadPool.getState() == ThreadPoolState.running)
            return;

        //the structure was shutdown, and we don't get any guarantee that the item is going to
        //be processed or dealt with. That is why we need to check if it is still there.
        if (!workQueue.remove(task)) {
            //we were lucky, the task could not be found on the queue anymore, meaning
            //that it was handeled (either because it was returned by the shutdownNow method
            //or because it is (being) processed.
            return;
        }

        //the task was still on the queue, it is removed now, so lets throw an
        //RejectedExecutionException to indicate that the execution has failed.
        throw new RejectedExecutionException();
    }

    private class ThreadPoolJobImpl implements ThreadPoolJob<Runnable> {

        public Runnable getWork() throws InterruptedException {
            return workQueue.take();
        }

        public Runnable getShuttingdownWork() throws InterruptedException {
            return workQueue.poll(0, TimeUnit.NANOSECONDS);
        }

        public boolean executeWork(Runnable task) {
            task.run();
            return true;
        }
    }
}
