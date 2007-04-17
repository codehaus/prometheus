/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.lendablereference.LendableReference;
import org.codehaus.prometheus.lendablereference.RelaxedLendableReference;
import org.codehaus.prometheus.lendablereference.StrictLendableReference;
import org.codehaus.prometheus.uninterruptiblesection.TimedUninterruptibleSection;
import org.codehaus.prometheus.util.Latch;
import org.codehaus.prometheus.util.StandardThreadFactory;
import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The default implementation of the {@link RepeaterService} interface that uses a pool of threads
 * to keep repeating the task.
 * <p/>
 * todo:
 * strict en non strict repeat functionaliteit testen.
 * <p/>
 * The threads are not automatically created when a ThreadPoolRepeater is constructed. Only when a
 * task is repeated, or when the start method is called, the threadpool is filled.
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
 * out of order put. This can be solved by using a {@link org.codehaus.prometheus.resequencer.Resequencer}.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater implements RepeaterService {

    private final static PoisonRepeatable POISON_REPEATABLE = new PoisonRepeatable();



    /**
     * Creates a new StrictLendableReference
     *
     * @param task the task to be placed in the LendableReference. The task can be null.
     * @return the created StrictLendableReference.
     */
    public static LendableReference<Repeatable> newDefaultLendableRef(Repeatable task) {
        return newLendableRef(true, task);
    }

    /**
     * Creates a new unfair ReentrantLock. See {@link ReentrantLock#ReentrantLock()}
     *
     * @return the created Lock.
     */
    public static Lock newDefaultMainLock() {
        return new ReentrantLock();
    }

    /**
     * Creates a new default ThreadFactory.
     *
     * @return a newly created default ThreadFactory.
     */
    public static ThreadFactory newDefaultThreadFactory() {
        return new StandardThreadFactory("repeater");
    }

    /**
     * Creates a new LendableReference.
     *
     * @param strict the LendableReference should be relaxed or strict.
     * @param task   the task that is placed in the LendableReference. This value is allowed to be null.
     * @return the constructed LendableReference.
     */
    public static LendableReference<Repeatable> newLendableRef(boolean strict, Repeatable task) {
        if (strict)
            return new StrictLendableReference<Repeatable>(task);
        else
            return new RelaxedLendableReference<Repeatable>(task);
    }


    private final ThreadFactory threadFactory;
    private final LendableReference<Repeatable> lendableRef;
    private final Lock mainLock;
    private final Latch shutdownLatch;
    private final Set<Thread> threadpool = new HashSet<Thread>();
    //the desired poolsize.
    private final AtomicInteger poolsize;
    private volatile RepeaterServiceState state = RepeaterServiceState.Unstarted;

    /**
     * Creates a new ThreadPoolRepeater with the given poolsize.
     * <p/>
     * The ThreadPoolRepeater is not started and the threadpool is not filled when the constructor
     * completes, see {@link #start()}.
     *
     * @param poolsize the initial number of threads in the threadpool.
     * @throws IllegalArgumentException if poolsize smaller than 0.
     */
    public ThreadPoolRepeater(int poolsize) {
        this(newDefaultThreadFactory(), newDefaultMainLock(), newDefaultLendableRef(null), poolsize);
    }

    /**
     * Creates a new ThreadPoolRepeater with the given task, and poolsize.
     * <p/>
     * The ThreadPoolRepeater is not started and the threadpool is not filled when the constructor
     * completes, see {@link #start()}.
     *
     * @param task     the task to repeat.
     * @param poolsize the initial number of threads in the threadpool.
     * @throws IllegalArgumentException if poolsize smaller than 0.
     */
    public ThreadPoolRepeater(Repeatable task, int poolsize) {
        this(newDefaultThreadFactory(), newDefaultMainLock(), newDefaultLendableRef(task), poolsize);
    }

    /**
     * Creates a new ThreadPoolRepeater.
     * <p/>
     * The ThreadPoolRepeater is not started and the threadpool is not filled when the constructor
     * completes, see {@link #start()}.
     *
     * @param strict        if the ThreadPoolExecutor should be strict (true) or relaxed (false).
     * @param task          the task to repeat.
     * @param poolsize      the initial number of threads in the threadpool.
     * @param threadFactory the ThreadFactory that is used to populate the threadpool.
     * @throws NullPointerException if threadFactory is null.
     */
    public ThreadPoolRepeater(boolean strict, Repeatable task, int poolsize, ThreadFactory threadFactory) {
        this(threadFactory, newDefaultMainLock(), newLendableRef(strict, task), poolsize);
    }

    /**
     * Creates a new ThreadPoolRepeater.
     * <p/>
     * The ThreadPoolRepeater is not started and the threadpool is not filled when the constructor
     * completes, see {@link #start()}.
     *
     * @param threadFactory the ThreadFactory that is used to populate the threadpool.
     * @param mainLock      the mainLock
     * @param lendableRef   the reference used (should not contain a value).
     * @param poolsize      the initial number of threads in the threadpool.
     * @throws NullPointerException     if threadFactory or lendableRef is null.
     * @throws IllegalArgumentException if poolsize smaller than 0.
     */
    public ThreadPoolRepeater(ThreadFactory threadFactory, Lock mainLock,
                              LendableReference<Repeatable> lendableRef, int poolsize) {
        if (threadFactory == null || lendableRef == null) throw new NullPointerException();
        if (poolsize < 0) throw new IllegalArgumentException();

        this.threadFactory = threadFactory;
        this.lendableRef = lendableRef;
        this.poolsize = new AtomicInteger(poolsize);
        this.mainLock = mainLock;
        this.shutdownLatch = new Latch(mainLock);
    }


    public ExceptionHandler getExceptionHandler() {
        throw new RuntimeException();
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        if(exceptionHandler == null)throw new NullPointerException();
        throw new RuntimeException();
    }



    /**
     * Returns the ThreadFactory this ThreadPoolRepeater uses to fill the threadpool.
     *
     * @return the ThreadFactory this ThreadPoolRepeater uses to fill the
     *         threadpool.
     */
    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    /**
     * Returns the LendableReference this ThreadPoolExecutor uses to get the task to execute from.
     *
     * @return the LendableReference this ThreadPoolRepeater uses to get the task to execute from.
     */
    public LendableReference<Repeatable> getLendableRef() {
        return lendableRef;
    }

    /**
     * Returns the mainLock.
     *
     * @return the mainLock.
     */
    public Lock getMainLock() {
        return mainLock;
    }

    /**
     * Returns the shutdown Latch. The Latch should not be modified from the outside.
     *
     * @return the shutdownNow Latch.
     */
    public Latch getShutdownLatch() {
        return shutdownLatch;
    }

    public RepeaterServiceState getState() {
        return state;
    }

    interface PlaceTask {
        void place() throws InterruptedException, TimeoutException;
    }


    public void shutdown() {
        throw new RuntimeException();
    }

    private void repeat(PlaceTask placeTask) throws InterruptedException, TimeoutException {
        //there is a performance problem here: while the put takes place, the lock is held
        //and nobody can change the state (for example shutting down). This problem needs to
        //be fixed.

        mainLock.lockInterruptibly();

        try {
            switch (state) {
                case Unstarted:
                    placeTask.place();
                    internalStart();
                    break;
                case Running:
                    placeTask.place();
                    break;
                case Shuttingdown: {
                    String msg = "Can't repeat task, the ThreadPoolExecutor is shutting down";
                    throw new RejectedExecutionException(msg);
                }
                case Shutdown: {
                    String msg = "Can't repeat task, the ThreadPoolExecutor is shutdown";
                    throw new RejectedExecutionException(msg);
                }
                default:
                    throw new RuntimeException("unhandeled state: " + state);
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * If the ThreadPoolExecutor is not started yet, it is started.
     *
     * @param task {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     */
    public void repeat(final Repeatable task) throws InterruptedException {
        PlaceTask placeTask = new PlaceTask() {
            public void place() throws InterruptedException {
                lendableRef.put(task);
            }
        };

        try {
            repeat(placeTask);
        } catch (TimeoutException e) {
            throw new RuntimeException("should not happen", e);
        }
    }

    public boolean tryRepeat(final Repeatable task) {
        TimedUninterruptibleSection section = new TimedUninterruptibleSection() {
            protected Object originalsection(long timeoutNs) throws InterruptedException, TimeoutException {
                tryRepeat(task, timeoutNs, TimeUnit.NANOSECONDS);
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

    public void tryRepeat(final Repeatable task, final long timeout, final TimeUnit unit)
            throws TimeoutException, InterruptedException {
        PlaceTask placeTask = new PlaceTask() {
            public void place() throws TimeoutException, InterruptedException {
                lendableRef.tryPut(task, timeout, unit);
            }
        };

        repeat(placeTask);
    }

    public void shutdownNow() {
        mainLock.lock();
        try {
            switch (state) {
                case Unstarted:
                    internalShutdown();
                    break;
                case Running:
                    internalShutdown();
                    break;
                case Shuttingdown:
                    //nothing special needs to be done, it already is shutting down.
                    break;
                case Shutdown:
                    //nothing special needs to be done, it already is shut down.
                    break;
                default:
                    throw new RuntimeException("unhandeled state:" + state);
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Shuts down this Repeater.
     * <p/>
     * This call only should be made when the mainLock is held.
     * <p/>
     * No checking is done on the state, this is a responsibility of the caller.
     * <p/>
     * If there are no threadpool, the state is going to be 'shutdown.
     */
    private void internalShutdown() {
        if (threadpool.isEmpty()) {
            //there are no workers, so this repeater is shut down.
            updateState(RepeaterServiceState.Shutdown);
        } else {
            //there are workers, so change the status to shutting down. As soon as the last
            //worker has completes, this repeater is completely shutdown.
            updateState(RepeaterServiceState.Shuttingdown);
            interruptWorkers();
        }
    }

    /**
     * Interrupts all workers.
     * <p/>
     * This call only should be made when the mainLock is held.
     */
    private void interruptWorkers() {
        for (Thread t : threadpool)
            t.interrupt();
    }

    public void start() throws IllegalStateException {
        mainLock.lock();
        try {
            switch (state) {
                case Unstarted:
                    internalStart();
                    break;
                case Running:
                    //nothing special needs to be done because this
                    //ThreadPoolRepeater already is started.
                    return;
                case Shuttingdown: {
                    String msg = "this ThreadPoolRepeater can't be started, it already is shutting down";
                    throw new IllegalStateException(msg);
                }
                case Shutdown: {
                    String msg = "this ThreadPoolRepeater can't be started, it already is shut down";
                    throw new IllegalStateException(msg);
                }
                default:
                    throw new RuntimeException("unhandeled state: " + state);
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Starts this repeater.
     * <p/>
     * The call only should be made if the mainlock is hold.
     * <p/>
     * If the repeater is not in the unstarted state, a assertion error will be thrown.
     */
    private void internalStart() {
        assert state==RepeaterServiceState.Unstarted;

        for (int k = 0; k < poolsize.intValue(); k++) {
            Thread thread = createAndRegisterWorkerThread();
            thread.start();
        }
        updateState(RepeaterServiceState.Running);
    }

    /**
     * Creates a new unstarted worker thread.
     * <p/>
     * The thread is added to the threadpool.
     *
     * @return the newly created unstarted worker thread.
     */
    private Thread createAndRegisterWorkerThread() {
        Worker worker = new Worker();
        Thread thread = threadFactory.newThread(worker);
        worker.thread = thread;
        threadpool.add(thread);
        return thread;
    }

    public void awaitShutdown() throws InterruptedException {
        shutdownLatch.await();
    }

    public void tryAwaitShutdown(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        shutdownLatch.tryAwait(timeout, unit);
    }

    /**
     * Returns the actual number of threads. This value could be stale at the moment it is returned
     * because the number of running threads could have been changed when:
     * <ol>
     * <li>a workerthread that notices it should terminate</li>
     * <li>a different thread makes a call to {@link #setPoolSize(int)}</li>
     * </ol>
     *
     * @return the actual number of threads.
     * @see #getPoolSize()
     */
    public int getActualPoolSize() {
        mainLock.lock();

        try {
            return threadpool.size();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Returns the size of the threadpool.
     *
     * @return the size of the threadpool
     */
    public int getPoolSize() {
        return poolsize.intValue();
    }

    public void setPoolSize(int newPoolsize) {
        if (newPoolsize < 0)
            throw new IllegalArgumentException();

        mainLock.lock();
        try {
            switch (state) {
                case Unstarted:
                    //this repeater isn't running, so the newPoolsize can be changed without
                    //extra administration.
                    poolsize.set(newPoolsize);
                    break;
                case Running:
                    setPoolSizeInternally(newPoolsize);
                    break;
                case Shuttingdown:
                    throw new IllegalStateException();
                case Shutdown:
                    throw new IllegalStateException();
                default:
                    throw new RuntimeException("unhandeled state:" + state);
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Changes the number of workers in the pool.
     * <p/>
     * This call should only be made when the system is running, pausing, or paused.
     * <p/>
     * no state checking is done. This is a responsibility of the caller.
     * <p/>
     * mainLock should be held when executing this call.
     *
     * @param newpoolsize the new poolsize
     */
    private void setPoolSizeInternally(int newpoolsize) {
        int growSize = newpoolsize - poolsize.intValue();
        if (growSize == 0)
            return;//there is no change, so return from this call.

        poolsize.set(newpoolsize);

        if (growSize > 0) {
            //extra threads need to be created.
            for (int k = 0; k < growSize; k++) {
                Thread t = createAndRegisterWorkerThread();
                t.start();
            }
        } else {
            //if the growSize is smaller than zero (the number of workers needs to be reduced),
            //workerthreads are going to terminate themselves. So nothing needs to be done
            //here, it is up to the workers now to reduce their number.
        }
    }

    /**
     * This call should only be made if the mainLock is hold.
     *
     * @param state the new state
     */
    private void updateState(RepeaterServiceState state) {
        switch (state) {
            case Shutdown:
                shutdownLatch.openWithoutLocking();
                break;
        }

        this.state = state;
    }

    private class Worker implements Runnable {

        //the thread that executes this Worker.
        private volatile Thread thread;

        /**
         * Executes the task. The execution of the task is protected against runtime exceptions
         * (they are caught and dropped). If the task throws a RuntimeException, true is also
         * returned.
         *
         * @param task the Repeatable to execute
         * @return true if the task can be executed another time, false otherwise.
         */
        private boolean execute(Repeatable task) {
            try {
                return task.execute();
            } catch (Exception ex) {
                //Exception is being caught and ignored. It is up to the task
                //to deal with the exception. The java.util.prometheus.ThreadPoolExecutor
                //has a afterExecute mechanism that makes it possible to handle exceptions,
                //maybe this would be a feature for a future release. And injecting some
                //sort of handler instead of subclassing would be my preferred solution.
                return true;
            }
        }

        /**
         * Makes sure that this Worker doesn't have to be terminated.
         */
        private boolean ensureNotTerminated() {
            mainLock.lock();
            try {
                if (tooManyWorkers() || isShuttingdown()) {
                    workerDone();
                    return false;
                }

                return true;
            } finally {
                mainLock.unlock();
            }
        }

        public void run() {
            boolean again = true;
            do {
                try {
                    again = ensureNotTerminated() && executeOnce();
                } catch (InterruptedException e) {
                    //if the interrupt is not caused by a shutdown action.
                    //it can be ignored.
                    if (isShuttingdown()){
                        workerDone();
                        again = false;
                    }
                }finally{
                    Thread.interrupted();
                }
            } while (again);
        }

        /**
         * Returns true doesn't need to be terminated, false otherwise.
         *
         * @return
         */
        private boolean executeOnce() throws InterruptedException {
            //if the threadpoolrepeater shuts down, it will interrupt
            //all workers, if a workers is sleeping on this take, it
            //will be interrupted. And the task won't be executed.
            Repeatable task = lendableRef.take();
            boolean again = true;
            try {
                again = execute(task);
            } finally {
                if (again)
                    lendableRef.takeback(task);
                else {
                    lendableRef.takebackAndReset(task);
                }
            }
            return true;
        }

        private boolean tooManyWorkers() {
            return threadpool.size() > poolsize.intValue();
        }

        private boolean isShuttingdown() {
            return state == RepeaterServiceState.Shuttingdown;
        }

        /**
         * Terminates the worker. The thread that executes the worker is removed from the threadpool, and
         * if it is the last worker, the state of this repeater is updated to Shutdown.
         */
        private void workerDone() {
            mainLock.lock();
            try {
                threadpool.remove(thread);

                //if this is the last thread, this repeater now completely is shut down.
                if (threadpool.isEmpty() && state == RepeaterServiceState.Shuttingdown)
                    updateState(RepeaterServiceState.Shutdown);
            } finally {
                mainLock.unlock();
            }
        }
    }

    static class TerminatedException extends Exception {
    }

    static class PoisonRepeatable implements Repeatable {
        public boolean execute() {
            return false;
        }
    }
}
