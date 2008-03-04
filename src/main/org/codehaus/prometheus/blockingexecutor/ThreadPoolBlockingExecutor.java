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
import org.codehaus.prometheus.util.StandardThreadFactory;

import static java.lang.String.format;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link BlockingExecutorService} implementation that uses a {@link ThreadPool} for thread
 * management and a workqueue (a {@link BlockingQueue}) to store unprocessed jobs.
 * <p/>
 * <h1>Out of order execution</h1>
 * Even though a blocking queue is used to store the items, and an item won't be taken out of order
 * (FIFO contract) it could be that items are processed out of order for different reasons:
 * <ol>
 * <li>a taken task takes a longer time than others to execute</li>
 * <li>a taken task hasn't had the time to execute (maybe unlucky context switches).</li>
 * </ol>
 * The FIFO contract can also be broken, check the {@link PriorityBlockingQueue}. Using a single thread
 * would solve this problem (but isn't always the best solution).
 * <p/>
 * <h1>Bounded workqueue</h1>
 * <p/>
 * If you don't have control on the number of tasks in the workqueue, it could lead to resource problems
 * like running out of memory. To ensure graceful degradation, it is better to use a bounded BlockingQueue.
 * An unbounded BlockingQueue would not lead to blocking anyway..
 * </p>
 * <h1>Workqueue without internal capacity</h1>
 * <p/>
 * In some cases you don't want any unprocessed work, if that is the case, you can use a
 * {@link java.util.concurrent.SynchronousQueue} as workqueue. A SynchronousQueue only accepts tasks if
 * there is a worker thread waiting for it. If no worker thread is available, the submission of the
 * task blocks.
 * </p>
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public class ThreadPoolBlockingExecutor implements BlockingExecutorService {

    public static ThreadPool createDefaultThreadPool(ThreadFactory threadFactory) {
        return new StandardThreadPool(threadFactory);
    }

    public static BlockingQueue<Runnable> createDefaultWorkQueue() {
        return new LinkedBlockingQueue<Runnable>();
    }

    private final static AtomicLong defaultThreadPoolNameCounter = new AtomicLong(1);

    public static ThreadPool createDefaultThreadPool() {
        String poolname = "blockingexecutor#" + defaultThreadPoolNameCounter.incrementAndGet();
        ThreadFactory threadFactory = new StandardThreadFactory(poolname);
        return createDefaultThreadPool(threadFactory);
    }

    private final ThreadPool threadPool;
    private final BlockingQueue<Runnable> workQueue;
    private final ThreadPoolJobImpl job = new ThreadPoolJobImpl();
    private volatile int desiredPoolsize;
    private int threadsToTerminateCounter = 0;

    /**
     * Creates a ThreadPoolBlockingExecutor with the given poolsize.
     *
     * @param poolsize the initial number of threads in the ThreadPool.
     * @throws IllegalArgumentException if poolsize is smaller than 0.
     */
    public ThreadPoolBlockingExecutor(int poolsize) {
        this(poolsize, createDefaultThreadPool(), createDefaultWorkQueue());
    }

    /**
     * Creates a ThreadPoolBlockingExecutor with the given poolsize, ThreadPool
     * and workqueue.
     *
     * @param poolsize  the initial number of threads in the threadpool
     * @param factory   the ThreadFactory responsible for filling the threadpool
     * @param workQueue the BlockingQueue used to store unprocessed work.
     * @throws IllegalArgumentException if poolsize is smaller than 0.
     * @throws NullPointerException     if factory or workQueue is null.
     */
    public ThreadPoolBlockingExecutor(int poolsize, ThreadFactory factory, BlockingQueue<Runnable> workQueue) {
        this(poolsize, createDefaultThreadPool(factory), workQueue);
    }

    /**
     * Creates a ThreadPoolBlockingExecutor with the given ThreadPool and workqueue.
     *
     * @param poolsize   the number of threads in the pool
     * @param threadPool the ThreadPool that is used to manage threads.
     * @param workQueue  the BlockingQueue used to store unprocessed work.
     * @throws NullPointerException if threadPool or workQueue is null.
     */
    public ThreadPoolBlockingExecutor(int poolsize, ThreadPool threadPool, BlockingQueue<Runnable> workQueue) {
        if (threadPool == null || workQueue == null) throw new NullPointerException();

        this.threadPool = threadPool;
        this.workQueue = workQueue;
        this.threadPool.spawnWithoutStarting(job, poolsize);
        this.desiredPoolsize = poolsize;
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
        return desiredPoolsize;
    }

    /**
     * Sets the desired number of threads in the ThreadPool.
     *
     * @param poolSize the desired number of threads in the ThreadPool.
     * @throws IllegalArgumentException if poolsize smaller than 0.
     * @throws IllegalStateException    if the ThreadPool already is shutting down, or is shut down.
     */
    public void setDesiredPoolSize(int poolSize) {
        threadPool.getStateChangeLock().lock();
        try {
            if (threadPool.getState() != ThreadPoolState.running)
                throw new IllegalStateException();

            int extraThreads = poolSize - desiredPoolsize;
            if (extraThreads == 0)
                return;

            desiredPoolsize = poolSize;
            if (extraThreads > 0) {
                threadPool.spawn(job, extraThreads);
            } else {
                throw new RuntimeException();
            }
        } finally {
            threadPool.getStateChangeLock().unlock();
        }
    }

    public ExceptionHandler getExceptionHandler() {
        return threadPool.getExceptionHandler();
    }

    public void setExceptionHandler(ExceptionHandler handler) {
        threadPool.setExceptionHandler(handler);
    }

    public List<Runnable> shutdownAndDrain() {
        ThreadPoolState previousState = threadPool.shutdownPolitly();
        if (previousState != ThreadPoolState.running)
            return Collections.EMPTY_LIST;

        //this is the first thread that calls the shutdown, and this means we can drain the workqueue
        return drainWorkQueue();
    }

    public List<Runnable> shutdownPolitly() {
        ThreadPoolState previousState = threadPool.shutdownPolitly();
        //if a different thread has already shut down the ThreadPool, an empty list
        //can be returned.
        if (previousState != ThreadPoolState.running)
            return Collections.EMPTY_LIST;

        return getActualPoolSize() == 0 ? drainWorkQueue() : Collections.EMPTY_LIST;
    }

    public List<Runnable> shutdownNow() {
        ThreadPoolState previousState = threadPool.shutdownNow();
        if (previousState != ThreadPoolState.running)
            return Collections.EMPTY_LIST;

        //this is the first thread that calls the shutdown, and this means we can drain the workqueue
        return drainWorkQueue();
    }

    /**
     * Drains all items from the workqueue. Execute-threads that are pending for placement, are not
     * required to have placed their item before this call being called. If this is the case,
     * their item will be placed on the workqueue. These execute-threads are responsible themselfes
     * for ensuring that the task is placed. See {@link #ensureHandeled(Runnable)} for more
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
            case shuttingdownforced://fall through
            case shuttingdownnormally:
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
        ensureRunning();
        workQueue.put(task);
        ensureHandeled(task);
    }

    public void tryExecute(Runnable task, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (task == null || unit == null) throw new NullPointerException();

        ensureNoTimeout(timeout);
        ensureRunning();
        if (!workQueue.offer(task, timeout, unit))
            throw new TimeoutException();
        ensureHandeled(task);
    }

    private void ensureRunning() {
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
    private void ensureHandeled(Runnable task) {
        //the structure is still running, the calling thread isn't responsible anymore
        //for the handling of the task.
        if (threadPool.getState() == ThreadPoolState.running)
            return;

        //the structure was shutdown, and we don't get any guarantee that the item is going to
        //be processed or dealt with. That is why we need to check if it is still there.
        if (!workQueue.remove(task)) {
            //we were lucky, the task could not be found on the queue anymore, meaning
            //that it was handeled (either because it was the workqueue got drained on
            //a shutdown or because it is being processed).
            return;
        }

        //the task was still on the queue, it is removed now, so lets throw a
        //RejectedExecutionException to indicate that the task was rejected.
        throw new RejectedExecutionException();
    }

    private class ThreadPoolJobImpl implements ThreadPoolJob<Runnable> {

        public Runnable takeWork() throws InterruptedException {
            //if the shutdown has been called before the getState:
            //  the getState returns shuttingdown, and a non blocking poll needs to be done
            //  to prevent a worker thread from waiting till end of time
            //if the shutdown has been called after the getState:
            //  the getState returns running, and a blocking call take is done. This thread
            //  is considered idle, so it is interrupted by the shutdown (all idle threads are
            //  interrupted.
            //in both cases, the thread is not going to block forever on taking work from the workqueue.

            if (threadPool.getState() == ThreadPoolState.running)
                return workQueue.take();
            else
                return workQueue.poll();
        }

        public boolean executeWork(Runnable task) {
            if (task == null)
                return false;

            task.run();

            threadPool.getStateChangeLock().lock();
            try {
                if (threadsToTerminateCounter == 0)
                    return true;

                threadsToTerminateCounter--;
                return false;
            } finally {
                threadPool.getStateChangeLock().unlock();
            }
        }
    }
}