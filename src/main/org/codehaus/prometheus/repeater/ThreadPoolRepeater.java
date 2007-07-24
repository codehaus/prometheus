/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.references.LendableReference;
import org.codehaus.prometheus.references.RelaxedLendableReference;
import org.codehaus.prometheus.references.StrictLendableReference;
import org.codehaus.prometheus.threadpool.StandardThreadPool;
import org.codehaus.prometheus.threadpool.ThreadPool;
import org.codehaus.prometheus.threadpool.ThreadPoolJob;
import org.codehaus.prometheus.threadpool.ThreadPoolState;
import org.codehaus.prometheus.uninterruptiblesection.TimedUninterruptibleSection;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

/**
 * The default implementation of the {@link RepeaterService} interface that {@link ThreadPool} to
 * manage threads.
 * <p/>
 * The threads are not automatically created when a ThreadPoolRepeater is constructed. Only when a
 * task is repeated, or when the spawned_start method is called, the threadpool is filled.
 * todo:
 * if the threadpoolrepeater is shutdown, what is the value
 * of the lendableref.
 * <p/>
 * <td><b>Strict or relaxed</b></td>
 * <dd>
 * A ThreadPoolRepeater can be strict or non strict. With a strict ThreadPoolRepeater it isn't
 * possible that different runnable's are running at the same time. With the relaxed
 * ThreadPoolRepeater this is possible. This is implemented by using different LendableReferences:
 * <ol>
 * <li><b>strict ThreadPoolRepeater</b> is realized by using a {@link StrictLendableReference}</li>
 * <li><b>relaxed ThreadPoolRepeater</b> is realized by using a {@link RelaxedLendableReference}</li>
 * </ol>
 * The consequence of a strict ThreadPoolRepeater, is that there is more lock contention when the
 * task is often changed. If prometheus execution of different tasks is not an issue, a relaxed
 * ThreadPoolRepeater would be a better performing alternative.
 * </dd>
 * <p/>
 * <td><b>Pausing and throttling</b></td>
 * <dd>
 * The ThreadPoolRepeater doesn't provide pause functionality out of the box, but it can be added by
 * customizing the LendableReference, example:
 * <pre>
 * LendableReference target = new RelaxedLendableReference();
 * CloseableWaitpoint closeableWaitpoint = new CloseableWaitpoint();
 * LendableReference lendableRef = new LendableReferenceWithWaitingTakes(target,closeableWaitpoint);
 * ThreadPoolRepeater repeater = new ThreadPoolRepeater(..,..,lendableRef,...);
 * </pre>
 * By closing the closeableWaitpoint, you can prevent workerthreads from taking a task to execute
 * (so the Repeater is pausing). I'm thinking about integrating this in the ThreadPoolRepeater in
 * one of the next releases.
 * <p/>
 * The same technique can be used to throttle the Repeater (eg max 10 executions per minute) by
 * using a throttling waitpoint.
 * <p/>
 * reminder: if the threadpoolrepeater shuts down, it should unpause because else worker threads
 * won't have the chance to shut down. Or are the interrupted at shutdown, breaking through the
 * lendablereference.take method if they are blocking.
 * </dd>
 * <p/>
 * <td><b>Dealing with RuntimeExceptions</b></td>
 * <dd>
 * At the moment the RuntimeExceptions that are thrown by the repeating-task, are caught and
 * discarded. If exception handling is needed, you have to place it in the Runnable. Maybe
 * functionality will be added for it in the future. Unlike the ThreadPoolRepeater, a subclass
 * doesn't have to be created, some sort of handler can be injected instead.
 * </dd>
 * <td><b>ThreadPoolRepeater vs ThreadPoolExecutor</b></td>
 * <dd>
 * Repeating a task could also be realized by using a {@link java.util.concurrent.ThreadPoolExecutor}
 * and creating a special {@link java.util.concurrent.BlockingQueue} as workqueue that keeps handing
 * out the same task to the workerthreads, without that task being removed from the workqueue. I
 * don't know if that is a good approach. Concurrency control is a very complex subject and
 * personally I like to have components that clearly define their intention. In this case I find the
 * Repeater better describing the fact that a task is repeated than an Executor. Another difference
 * is that even with such a Queue it difficult to make the repeater strict because an
 * ThreadPoolRepeater has no notion of giving the executed task back. So it is much more difficult
 * to determine when a task has completed.
 * </dd>
 * <p/>
 * If multiple threads are used, you have to make sure that the task that is executed, is threadsafe.
 * If multiple threads are used, and items are taken, processed and than put, it could lead to an
 * out of order put. This can be solved by using some sort of Resequencer.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater implements RepeaterService {

    public static LendableReference<Repeatable> createDefaultLendableReference(Repeatable repeatable) {
        return new StrictLendableReference<Repeatable>(repeatable);
    }

    public static LendableReference<Repeatable> createDefaultLendableReference(boolean strict, Repeatable repeatable) {
        if (strict)
            return new StrictLendableReference<Repeatable>(repeatable);
        else
            return new RelaxedLendableReference<Repeatable>(repeatable);
    }

    public static ThreadPool createDefaultThreadpool(int poolsize, ThreadFactory threadFactory) {
        return new StandardThreadPool(poolsize, threadFactory);
    }

    public static ThreadPool createDefaultThreadpool(int poolsize) {
        return new StandardThreadPool(poolsize);
    }

    //fields are made protected so RepeatableExecutionStrategy has access to them
    protected final LendableReference<Repeatable> lendableRef;
    protected final ThreadPool threadPool;
    private volatile RepeatableExecutionStrategy repeatableExecutionStrategy = new EndTaskStrategy();

    /**
     * Creates a new strict and unstarted ThreadPoolRepeater with one thread and a
     * {@link org.codehaus.prometheus.util.StandardThreadFactory ()}.
     */
    public ThreadPoolRepeater() {
        this(createDefaultThreadpool(1), createDefaultLendableReference(null));
    }

    /**
     * Creates a new strict and unstarted ThreadPoolRepeater with the given poolsize and a
     * {@link org.codehaus.prometheus.util.StandardThreadFactory ()}.
     *
     * @param poolsize the desired number of worker-threads in the threadpool.
     * @throws IllegalArgumentException if poolsize smaller than 0
     */
    public ThreadPoolRepeater(int poolsize) {
        this(createDefaultThreadpool(poolsize), createDefaultLendableReference(null));
    }

    /**
     * Creates a new strict and unstarted ThreadPoolRepeater with a single thread and the given
     * task and a {@link org.codehaus.prometheus.util.StandardThreadFactory}.
     *
     * @param repeatable the task to repeat (is allowed to be null).
     */
    public ThreadPoolRepeater(Repeatable repeatable) {
        this(createDefaultThreadpool(1), createDefaultLendableReference(repeatable));
    }

    /**
     * Creates a new strict and unstarted ThreadPoolRepeater with the given poolsize and
     * task to repeat. It uses a {@link StandardThreadPool#StandardThreadPool()} as threadfactory.
     *
     * @param task     the task that should be repeater (is allowed to be null).
     * @param poolsize the desired number of worker-threads in the threadpool.
     * @throws IllegalArgumentException if poolsize smaller than 0.
     */
    public ThreadPoolRepeater(Repeatable task, int poolsize) {
        this(createDefaultThreadpool(poolsize), createDefaultLendableReference(task));
    }

    /**
     * Creates a new unstarted ThreadPoolRepeater with the given task, poolsize and ThreadFactory.
     *
     * @param strict        if repeater is strict or relaxed.
     * @param task          the task to repeat (is allowed to be null).
     * @param poolsize      the desired number of worker-threads in the threadpool.
     * @param threadFactory the ThreadFactory used to fill the threadpool.
     * @throws NullPointerException     if threadFactory is null.
     * @throws IllegalArgumentException if poolsize smaller than 0.
     */
    public ThreadPoolRepeater(boolean strict, Repeatable task, int poolsize, ThreadFactory threadFactory) {
        this(createDefaultThreadpool(poolsize, threadFactory), createDefaultLendableReference(strict, task));
    }

    /**
     * Creates a new unstarted ThreadPoolRepeater with the given threadPool and lendableReference.
     * The ThreadPoolRepeater also sets the ThreadPoolJob on the ThreadPool.
     *
     * @param threadPool  the ThreadPool this ThreadPoolRepeater uses to manage threads.
     * @param lendableRef the LendableReference that is used to store the task to repeat.
     * @throws NullPointerException if threadPool or lendableRef is null
     */
    public ThreadPoolRepeater(ThreadPool threadPool, LendableReference<Repeatable> lendableRef) {
        if (threadPool == null || lendableRef == null) throw new NullPointerException();
        this.threadPool = threadPool;
        this.threadPool.setWorkerJob(new RepeaterThreadPoolJob());
        this.lendableRef = lendableRef;
    }

    /**
     * Returns the LendableReference this ThreadPoolRepeater uses to store the reference to the
     * current task.
     *
     * @return the LendableReference this ThreadPoolRepeater uses.
     */
    public LendableReference<Repeatable> getLendableRef() {
        return lendableRef;
    }


    /**
     * Returns the ThreadPool this ThreadPoolRepeater uses.
     *
     * @return the ThreadPool this ThreadPoolRepeater uses.
     */
    public ThreadPool getThreadPool() {
        return threadPool;
    }

    /**
     * Sets the new RepeatableExecutionStrategy
     *
     * @param repeatableExecutionStrategy the new RepeatableExecutionStrategy
     * @throws NullPointerException if repeatableExecutionStrategy is null
     */
    public void setRepeatableExecutionStrategy(RepeatableExecutionStrategy repeatableExecutionStrategy) {
        if (repeatableExecutionStrategy == null) throw new NullPointerException();
        this.repeatableExecutionStrategy = repeatableExecutionStrategy;
    }

    /**
     * Gets the current RepeatableExecutionStrategy. The returned value will never be null.
     *
     * @return the current RepeatableExecutionStrategy
     */
    public RepeatableExecutionStrategy getRepeatableExecutionStrategy() {
        return repeatableExecutionStrategy;
    }

    public ExceptionHandler getExceptionHandler() {
        return threadPool.getExceptionHandler();
    }

    public void setExceptionHandler(ExceptionHandler handler) {
        threadPool.setExceptionHandler(handler);
    }

    public void start() {
        threadPool.start();
    }

    public void shutdown() {
        threadPool.shutdown();
    }

    public void shutdownNow() {
        threadPool.shutdownNow();
    }

    public void awaitShutdown() throws InterruptedException {
        threadPool.awaitShutdown();
    }

    public void tryAwaitShutdown(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        threadPool.tryAwaitShutdown(timeout, unit);
    }

    public int getActualPoolSize() {
        return threadPool.getActualPoolSize();
    }

    public int getDesiredPoolSize() {
        return threadPool.getDesiredPoolSize();
    }

    public void setDesiredPoolSize(int poolSize) {
        threadPool.setDesiredPoolsize(poolSize);
    }

    public RepeaterServiceState getState() {
        ThreadPoolState state = threadPool.getState();
        switch (state) {
            case unstarted:
                return RepeaterServiceState.unstarted;
            case running:
                return RepeaterServiceState.running;
            case shuttingdown:
                return RepeaterServiceState.shuttingdown;
            case forcedshuttingdown:
                return RepeaterServiceState.shuttingdown;
            case shutdown:
                return RepeaterServiceState.shutdown;
            default:
                throw new IllegalStateException("unhandeled state: " + state);
        }
    }

    public void repeat(Repeatable task) throws InterruptedException {
        ensureUsableRepeater();

        //it could be that the repeater just has begon shutting down,
        //or completely has shutdown. It is up to the task to figure out
        //if it is executed.
        lendableRef.put(task);
    }

    /**
     * Makes sure that there is ThreadPoolRepeater that is able to work. If it isn't
     * possible, a RejectedExecutionException is thrown.
     */
    private void ensureUsableRepeater() {
        Lock lock = threadPool.getStateChangeLock();
        lock.lock();
        try {
            ThreadPoolState state = threadPool.getState();
            switch (state) {
                case unstarted:
                    start();
                    break;
                case running:
                    break;
                case shuttingdown:
                    //fall through
                case forcedshuttingdown:
                    //fall through
                case shutdown:
                    throw new RejectedExecutionException();
                default:
                    throw new RuntimeException("unhandled state " + state);
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean tryRepeat(final Repeatable task) {
        ensureUsableRepeater();

        TimedUninterruptibleSection section = new TimedUninterruptibleSection() {
            protected Object originalsection(long timeoutNs) throws InterruptedException, TimeoutException {
                lendableRef.tryPut(task, timeoutNs, TimeUnit.NANOSECONDS);
                return null;
            }
        };

        try {
            section.tryExecute();
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void tryRepeat(Repeatable task, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        ensureUsableRepeater();

        //it could be that this method is called even though the threadpool is shutting down, or shut down.
        //this means that a task is placed, but not executed.
        lendableRef.tryPut(task, timeout, unit);
    }

    private class RepeaterThreadPoolJob implements ThreadPoolJob<Repeatable> {

        public Repeatable getWork() throws InterruptedException {
            return lendableRef.take();
        }

        public Repeatable getShuttingdownWork() throws InterruptedException {
            //no guarantees are made how many times a task is executed, so why try to retrieve
            //one if we aren't required to process it.
            return null;
        }

        public boolean executeWork(Repeatable task) throws Exception {
            return  repeatableExecutionStrategy.execute(task, ThreadPoolRepeater.this);
        }
    }
}
