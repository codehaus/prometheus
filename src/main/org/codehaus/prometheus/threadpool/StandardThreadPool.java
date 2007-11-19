/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.exceptionhandler.NoOpExceptionHandler;
import org.codehaus.prometheus.util.JucLatch;
import org.codehaus.prometheus.util.Latch;
import org.codehaus.prometheus.util.StandardThreadFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Default {@link ThreadPool} implementation.
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public class StandardThreadPool implements ThreadPool {

    private final static AtomicLong poolNameGeneratorCounter = new AtomicLong(1);

    private static StandardThreadFactory createDefaultThreadFactory() {
        return new StandardThreadFactory("threadpool" + poolNameGeneratorCounter.incrementAndGet());
    }

    private final Lock mainLock = new ReentrantLock();
    private final Latch shutdownLatch = new JucLatch();
    private final Set<Worker> workers = new HashSet<Worker>();
    private final ThreadFactory threadFactory;

    private volatile ThreadPoolJob threadPoolJob;
    private volatile ThreadPoolState state = ThreadPoolState.unstarted;
    private volatile int desiredPoolsize;
    private volatile ExceptionHandler exceptionHandler = NoOpExceptionHandler.INSTANCE;

    /**
     * Creates a new StandardThreadPool with a {@link StandardThreadFactory} as ThreadFactory
     * an no {@link ThreadPoolJob} and zero threads in the threadpool.
     */
    public StandardThreadPool() {
        this(createDefaultThreadFactory());
    }

    /**
     * Creates a new StandardThreadPool with a {@link StandardThreadFactory}, no {@link ThreadPoolJob}
     * and the given number of threads in the threadpool.
     *
     * @param poolsize the number of threads in the threadpool.
     * @throws IllegalArgumentException if poolsize is smaller than 0.
     */
    public StandardThreadPool(int poolsize) {
        this(poolsize, createDefaultThreadFactory());
    }

    /**
     * Creates a new StandardThreadPool with the given ThreadFactory and no
     * {@link ThreadPoolJob} and no threads in the threadpool.
     *
     * @param factory the ThreadFactory that is used to fill the pool.
     * @throws NullPointerException if factory is <tt>null</tt>.
     */
    public StandardThreadPool(ThreadFactory factory) {
        this(0, null, factory);
    }

    /**
     * Creates a new StandardThreadPool with the given poolsize and ThreadFactory.
     * The ThreadPoolJob needs to be set before this StandardThreadPool is running.
     *
     * @param poolsize      the number of threadsin the threadpool.
     * @param threadFactory the ThreadFactory that is used to fill the pool.
     * @throws IllegalArgumentException if poolsize smaller than zero.
     * @throws NullPointerException     if threadfactory is null.
     */
    public StandardThreadPool(int poolsize, ThreadFactory threadFactory) {
        this(poolsize, null, threadFactory);
    }

    /**
     * Creates a new StandardThreadPool with the given {@link ThreadFactory} and
     * threadPoolJob and no threads in the threadpool.
     *
     * @param threadPoolJob the job that should be executed. The value is allowed to be null, but needs to be
     *                      set before running.
     * @param factory       the ThreadFactory that is used to fill the pool.
     * @throws NullPointerException if threadPoolJob or factory is <tt>null</tt>.
     */
    public StandardThreadPool(ThreadPoolJob threadPoolJob, ThreadFactory factory) {
        this(0, threadPoolJob, factory);
    }

    /**
     * Creates a new StandardThreadPool with the given poolsize, ThreadFactory and workerjob.
     *
     * @param poolsize      the initial size of the threadpool.
     * @param threadPoolJob the job that should be executed. The value is allowed to be <tt>null</tt>, but
     *                      needs to be set before running.
     * @param factory       the ThreadFactory that is used to fill the pool.
     * @throws IllegalArgumentException if poolsize smaller than zero.
     * @throws NullPointerException     if factory is null.
     */
    public StandardThreadPool(int poolsize, ThreadPoolJob threadPoolJob, ThreadFactory factory) {
        if (poolsize < 0) throw new IllegalArgumentException();
        if (factory == null) throw new NullPointerException();
        this.threadPoolJob = threadPoolJob;
        this.threadFactory = factory;
        this.desiredPoolsize = poolsize;
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler handler) {
        if (handler == null) throw new NullPointerException();
        this.exceptionHandler = handler;
    }

    public ThreadPoolState getState() {
        return state;
    }

    /**
     * Changes the state this StandardThreadPool is in. All changes to the state should
     * be made through this method.
     * <p/>
     * Call only should be made when the main lock is held.
     *
     * @param newState the new ThreadPoolState
     * @return the old ThreadPoolState
     */
    private ThreadPoolState updateState(ThreadPoolState newState) {
        assert newState != null;

        ThreadPoolState oldState = state;
        state = newState;

        if (state == ThreadPoolState.shutdown)
            shutdownLatch.open();

        return oldState;
    }

    public int getActualPoolSize() {
        mainLock.lock();
        try {
            return workers.size();
        } finally {
            mainLock.unlock();
        }
    }

    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public ThreadPoolJob getJob() {
        return threadPoolJob;
    }

    public void setJob(ThreadPoolJob threadPoolJob) {
        if (threadPoolJob == null) throw new NullPointerException();

        mainLock.lock();
        try {
            if (state != ThreadPoolState.unstarted)
                throw new IllegalStateException("threadPoolJob can only be set on an unstarted ThreadPool");

            this.threadPoolJob = threadPoolJob;
        } finally {
            mainLock.unlock();
        }
    }

    public int getDesiredPoolSize() {
        return desiredPoolsize;
    }

    public void setDesiredPoolsize(int desiredPoolsize) {
        if (desiredPoolsize < 0) throw new IllegalArgumentException();

        mainLock.lock();
        try {
            switch (state) {
                case unstarted:
                    this.desiredPoolsize = desiredPoolsize;
                    break;
                case running:
                    int extraThreads = desiredPoolsize - workers.size();
                    if (extraThreads == 0)
                        return;

                    this.desiredPoolsize = desiredPoolsize;

                    if (extraThreads > 0)
                        createNewWorkers(extraThreads);
                    else
                        interruptIdleWorkers(-extraThreads);
                    break;
                case shuttingdownnormally:
                    throw new IllegalStateException("Can't change the poolsize, threadpool is shutting down");
                case shuttingdownforced:
                    throw new IllegalStateException("Can't change the poolsize, threadpool is forced shutting down");
                case shutdown:
                    throw new IllegalStateException("Can't change the poolsize, threadpool is shutdown");
                default:
                    throw new RuntimeException("unhandeled state:" + state);
            }
        } finally {
            mainLock.unlock();
        }
    }

    public void start() {
        mainLock.lock();
        try {
            switch (state) {
                case unstarted:
                    ensureThreadPoolJobAvailable();
                    updateState(ThreadPoolState.running);
                    createNewWorkers(desiredPoolsize);
                    break;
                case running:
                    //ignore call, threadpool already is running
                    return;
                case shuttingdownnormally:
                    throw new IllegalStateException("Can't start, threadpool is shutting down");
                case shuttingdownforced:
                    throw new IllegalStateException("Can't start, threadpool is forced shutting down");
                case shutdown:
                    throw new IllegalStateException("Can't start, threadpool is shutdown");
                default:
                    throw new RuntimeException("unhandled state: " + state);
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Makes sure that the the threadPoolJob is available. If it isn't available, an
     * IllegalStateException is thrown.
     */
    private void ensureThreadPoolJobAvailable() {
        if (threadPoolJob == null)
            throw new IllegalStateException("Can't start, no threadPoolJob is set");
    }

    /**
     * Tries to interrupt the given number of idle workers.
     * <p/>
     * Call only should be made when main lock is hold. This prevents other threads from calling
     * this method concurrently.
     *
     * @param count the number of idle workers to interrupt
     */
    private void interruptIdleWorkers(int count) {
        assert count >= 0;

        int interrupted = 0;
        for (Worker worker : workers) {
            if (worker.interruptIfIdle())
                interrupted++;

            //if the expected number of workers are interrupted, we are finished
            if (interrupted == count)
                return;
        }
    }

    /**
     * Interrupts all workers (no matter if they are idle or not).
     * <p/>
     * Call only should be made when the main lock is held.
     */
    private void interruptAllWorkers() {
        for (Worker worker : workers)
            worker.thread.interrupt();
    }

    /**
     * Interrupts all idle workers.
     * <p/>
     * Call only should be made when the main lock is held.
     */
    private void interruptAllIdleWorkers() {
        for (Worker worker : workers)
            worker.interruptIfIdle();
    }

    /**
     * Creates new workers.
     * <p/>
     * Call only should be made when the main lock is held.
     *
     * @param count the number of workers to create.
     */
    private void createNewWorkers(int count) {
        assert count >= 0;
        for (int k = 0; k < count; k++)
            createNewWorker();
    }

    /**
     * Creates and registers a new Worker. The created worker is added to the
     * set of workers. Actual reference to the Worker is not required for the moment,
     * hence the void return value.
     * <p/>
     * Call only should be made when the main lock is held.
     */
    private void createNewWorker() {
        assert state == ThreadPoolState.running;

        Worker worker = new Worker();
        Thread t = threadFactory.newThread(worker);
        worker.thread = t;
        workers.add(worker);
        t.start();
    }

    /**
     * @inheritDoc Every time this method is called, also all non idle threads are interrupted.
     */
    public ThreadPoolState shutdownNow() {
        return shutdown(true);
    }

    public ThreadPoolState shutdownPolitly() {
        return shutdown(false);
    }

    /**
     * @param forced if the shutdown is forced (so if non idle threads should be interrupted)
     * @return the previous state
     */
    private ThreadPoolState shutdown(boolean forced) {
        mainLock.lock();
        try {
            switch (state) {
                case unstarted:
                    return updateState(ThreadPoolState.shutdown);
                case running:
                    if (workers.isEmpty()) {
                        //if there are no workers, the threadpool can shutdown immediately.
                        return updateState(ThreadPoolState.shutdown);
                    }

                    //there are workers
                    if (forced) {
                        ThreadPoolState previousState = updateState(ThreadPoolState.shuttingdownforced);
                        interruptAllWorkers();
                        return previousState;
                    } else {
                        ThreadPoolState previousState = updateState(ThreadPoolState.shuttingdownnormally);
                        interruptAllIdleWorkers();
                        return previousState;
                    }
                case shuttingdownnormally:
                    //there are workers
                    if (forced) {
                        ThreadPoolState previousState = updateState(ThreadPoolState.shuttingdownforced);
                        interruptAllWorkers();
                        return previousState;
                    } else {
                        return state;
                    }
                case shuttingdownforced:
                    //this implementation allows for repeated interruptions of all 
                    //(idle and non idle) workers.
                    if (forced)
                        interruptAllWorkers();
                    return state;
                case shutdown:
                    return state;
                default:
                    throw new RuntimeException("unhandeled state: " + state);
            }
        } finally {
            mainLock.unlock();
        }
    }

    public void awaitShutdown() throws InterruptedException {
        shutdownLatch.await();
    }

    public void tryAwaitShutdown(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        shutdownLatch.tryAwait(timeout, unit);
    }

    private class Worker implements Runnable {
        private final Lock isExecutingWorkLock = new ReentrantLock();
        //the thread belongs-to/executes this Worker.
        private volatile Thread thread;

        public void run() {
            try {
                executeWorkLoopWhileRunning();
                executeWorkLoopWhileShuttingdownNormally();
            } finally {
                workerCompleted();
            }
        }

        /**
         * todo: logica met mainLoopforshuttingdown is grotendeels zelfde.. maybe extract template to capture logic?
         * <p/>
         * This is the loop the workers keep executing when the threadpool is
         * running.
         */
        private void executeWorkLoopWhileRunning() {
            //loop that processes all work for shutting down.
            while (state == ThreadPoolState.running) {
                //if the worker is not wanted anymore, the worker can end itself after this call is made,
                //the thread isn"t visible for the outside world anymore
                if (removeIfTooManyThreads())
                    break;

                Object work;
                try {
                    work = threadPoolJob.takeWork();
                } catch (InterruptedException e) {
                    //The thread can be interrupted, while getting work, by the threadpool. The interrupt itself can
                    //be ignored. If the threadpool wants to shut down, the executeWorkLoopWhileRunning will be ended
                    //as soon as the poolthread enters the executeWorkLoopWhileRunning again. If the thread was
                    //interrupted otherwise, the interrupt is lost. This behavior is similar to the ThreadPoolExecutor.

                    work = null;
                } catch (RuntimeException ex) {
                    //todo : handle exception
                    ex.printStackTrace();
                    work = null;
                }

                //execute the work if some was found
                if (work != null) {
                    //if the execution of the work returned false, the worker can end itself.
                    if (!letThreadPoolJobExecuteWork(work))
                        break;
                }
            }
        }

        /**
         * A loop that is executed by a worker when the ThreadPool shuts down normally.  This
         * loop will end as soon as the state is not equal to shuttingdownnormally or when
         * the threadPoolJob#takeWorkForNormalShutdown returns null.
         */
        private void executeWorkLoopWhileShuttingdownNormally() {
            while (state == ThreadPoolState.shuttingdownnormally) {
                Object work;
                try {
                    work = threadPoolJob.takeWorkForNormalShutdown();
                    //if null was returned, there was no work to execute and this loop can be ended
                    if (work == null)
                        break;
                } catch (InterruptedException ex) {
                    //ignore it. If the worker was interrupted while getting work for a normal shutdown
                    //and this interrupt was caused by a forced shutdown, the loop will exit.
                    work = null;
                } catch (RuntimeException ex) {
                    //it could be that the  takeWorkForNormalShutdown has thrown an exception. The exception
                    //is print so it isn't eaten. The exceptionhandler is not called because I don't know
                    //if they should contain logic.
                    ex.printStackTrace();
                    work = null;
                }

                //if work was retrieved, execute it.
                if (work != null) {
                    letThreadPoolJobExecuteWork(work);
                }
            }
        }


        /**
         * Lets the Worker execute the Work. The work is executed under a isExecutingWorkLock that marks it as
         * 'running' and prevents it from being seen as idle (and being interrupted while idle).
         * <p/>
         * All exceptions are caught and send to the exceptionHandler. Other throwable's, like Error,
         * or not caught and could potentially damage the ThreadPool internals. The exceptionhandler
         * is also called under the same isExecutingWorkLock. Meaning that it also
         * won't be seen as an idle action. If an exceptionhandler throws an exception, this exception
         * is gobbled up.
         *
         * @param work the task to execute.
         * @return true if the worker should execute again, false if the worker should end the main loop.
         */
        private boolean letThreadPoolJobExecuteWork(Object work) {
            isExecutingWorkLock.lock();
            try {
                return threadPoolJob.executeWork(work);
            } catch (Exception ex) {
                handleException(ex);
                return true;
            } finally {
                removeInterruptStatus();
                isExecutingWorkLock.unlock();
            }
        }

        /**
         * Removes the Worker if it is unwanted. A Worker is unwanted when the ThreadPool when there the
         * actual poolsize is larger than the desired poolsize.
         *
         * @return true if Worker was unwanted (and therefor removed) or false if it still is wanted.
         */
        private boolean removeIfTooManyThreads() {
            assert state == ThreadPoolState.running ||
                    state == ThreadPoolState.shuttingdownnormally ||
                    state == ThreadPoolState.shuttingdownforced;

            mainLock.lock();
            try {
                if (hasTooManyWorkers()) {
                    //there were too many threads in the pool, so remove this thread. If the worker is not
                    //removed here, another worker could also think he should be removed when he discoveres
                    //that there are too many threads and this means that too many threads terminate.

                    workers.remove(this);
                    //return true to indicate that the worker is unwanted
                    return true;
                } else {
                    //the pool is not too large, and it it still running, so return false to indicate
                    //that the worker is not unwanted
                    return false;
                }
            } finally {
                mainLock.unlock();
            }
        }

        /**
         * Checks if there are too many workers (if there are more threads in the threadpool
         * than 'desiredPoolSize'. Call only should be made when mainLock is hold.
         *
         * @return true if there are too many workers, false otherwise.
         */
        private boolean hasTooManyWorkers() {
            return workers.size() > desiredPoolsize;
        }

        /**
         * Interrupts the thread only if isn't executing a threadPoolJob. It doesn't meant that the worker actually
         * is interrupted (it depends on the implementation of {@link ThreadPoolJob#takeWork()}.
         * <p/>
         * This call is not made by the worker Thread (unless we the worker calls back on the threadpool)
         * but by the thread that callls an uperation like shutdown
         *
         * @return true if the thread was idle, false otherwise.
         */
        private boolean interruptIfIdle() {
            //if the isExecutingWorkLock is available, the worker isn't running it can be interrupted.
            if (isExecutingWorkLock.tryLock()) {
                try {
                    thread.interrupt();
                    //interrupt was success.
                    return true;
                } finally {
                    isExecutingWorkLock.unlock();
                }
            } else {
                //the lock could not be obtained because the worker is executing work and not idle
                //and this means that it should not be interrupted.  It returns false to indicate
                //that the interrupt didn't happen.
                return false;
            }
        }

        /**
         * The ExceptionHandler is executed under a try/catch meaning that an exception handler that throws an
         * exception won't corrupt the threadpool. If this happens, the Stacktrace is print in the System.err
         *
         * @param exception the Exception to handle.
         */
        private void handleException(Exception exception) {
            //the exceptionHandler also is used under the isExecutingWorkLock.
            try {
                exceptionHandler.handle(exception);
            } catch (RuntimeException handlerException) {
                handlerException.printStackTrace();
            }
        }

        /**
         * Removes the interrupt status from the calling thread (should be the same as the thread that is
         * executing this Worker).
         */
        private void removeInterruptStatus() {
            assert Thread.currentThread() == thread;
            Thread.interrupted();
        }

        /**
         * Does cleanup when a worker terminates. If it is the last worker and the ThreadPool isn't in
         * the running state anymore, the threadpool is put in the shutdown state (the last one that
         * leaves the building should turn off the lights).
         */
        private void workerCompleted() {
            mainLock.lock();
            try {
                //it could be that the thread already is removed, so the remove could return false.
                workers.remove(this);

                if (workers.isEmpty() && state.isShuttingdownState())
                    updateState(ThreadPoolState.shutdown);
            } finally {
                mainLock.unlock();
            }
        }
    }
}