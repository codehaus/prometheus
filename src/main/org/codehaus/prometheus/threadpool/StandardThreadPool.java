/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.exceptionhandler.NullExceptionHandler;
import org.codehaus.prometheus.util.JucLatch;
import org.codehaus.prometheus.util.Latch;
import org.codehaus.prometheus.util.StandardThreadFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Default {@link ThreadPool} implementation.
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public class StandardThreadPool implements ThreadPool {

    private static StandardThreadFactory createDefaultThreadFactory() {
        return new StandardThreadFactory();
    }

    private final Lock mainLock = new ReentrantLock();
    private final Latch shutdownLatch = new JucLatch(mainLock);
    private final Set<Worker> workers = new HashSet<Worker>();
    private final ThreadFactory threadFactory;

    private volatile ThreadPoolJob threadPoolJob;
    private volatile ThreadPoolState state = ThreadPoolState.unstarted;
    private volatile int desiredPoolsize;
    private volatile ExceptionHandler exceptionHandler = NullExceptionHandler.INSTANCE;

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
        this.exceptionHandler = handler == null ? NullExceptionHandler.INSTANCE : handler;
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

    public Lock getMainLock() {
        return mainLock;
    }

    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public ThreadPoolJob getWorkerJob() {
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
                case shuttingdown:
                    throw new IllegalStateException("Can't change the poolsize, threadpool is shutting down");
                case forcedshuttingdown:
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
                    ensureWorkerJobAvailable();
                    updateState(ThreadPoolState.running);
                    createNewWorkers(desiredPoolsize);
                    break;
                case running:
                    //ignore call, threadpool already is running
                    return;
                case shuttingdown:
                    throw new IllegalStateException("Can't start, threadpool is shutting down");
                case forcedshuttingdown:
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
     * IllegalStateException is thrown. This call only should be made when the
     * mainLock is hold.
     */
    private void ensureWorkerJobAvailable() {
        if (threadPoolJob == null)
            throw new IllegalStateException("Can't start, nu threadPoolJob is set");
    }

    /**
     * Tries to interrupt the given number of idle workers.
     * <p/>
     * Call only should be made when main lock is hold.
     *
     * @param count the number of idle workers to interrupt
     */
    private void interruptIdleWorkers(int count) {
        assert count >= 0;

        int interrupted = 0;
        for (Worker worker : workers) {
            if (worker.interruptIfIdle())
                interrupted++;
            //if the expected number of workers are interrupted, this call can return.
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
            createWorker();
    }

    /**
     * Creates and registers a new Worker. The created worker is added to the
     * set of workers. Actual reference to the Worker is not required for the moment,
     * hence the void return value.
     * <p/>
     * Call only should be made when the main lock is held.
     */
    private void createWorker() {
        assert state == ThreadPoolState.running;

        Worker worker = new Worker();
        Thread t = threadFactory.newThread(worker);
        worker.thread = t;
        workers.add(worker);
        t.start();
    }

    /**
     * @inheritDoc Every time this method is called, all non idle threads are interrupted.
     */
    public ThreadPoolState shutdownNow() {
        return shutdown(true);
    }

    public ThreadPoolState shutdown() {
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
                    } else {
                        //there are workers
                        if (forced) {
                            interruptAllWorkers();
                            return updateState(ThreadPoolState.forcedshuttingdown);
                        } else {
                            interruptAllIdleWorkers();
                            return updateState(ThreadPoolState.shuttingdown);
                        }
                    }
                case shuttingdown:
                    if (forced) {
                        interruptAllWorkers();
                        return updateState(ThreadPoolState.forcedshuttingdown);
                    }
                    return ThreadPoolState.shuttingdown;
                case forcedshuttingdown:
                    //this implementation allows for repeated interruptions of all 
                    //(idle and non idle) workers.
                    if (forced)
                        interruptAllWorkers();
                case shutdown:
                    return ThreadPoolState.shutdown;
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
        private final Lock runningLock = new ReentrantLock();
        //the thread that executes this runnable.
        private volatile Thread thread;

        /**
         * todo: I'm not happy about the name..
         * <p/>
         * Returns true if the threadPoolJob should be run again, false otherwise.
         * The Thread also is removed from the ThreadPool if it is unwanted.
         *
         * @return true if the threadPoolJob should be run again, false otherwise
         */
        private boolean wantedWorkerCheck() {
            //this method can't be called if the threadpool is unstarted, of shutdown.
            assert state == ThreadPoolState.running ||
                    state == ThreadPoolState.shuttingdown ||
                    state == ThreadPoolState.forcedshuttingdown;

            mainLock.lock();
            try {
                //if the threadpool is shutting down, we don't want workers to be terminated.
                //Workers are going to terminate themselves when there is nothing more to do
                //for them (so when getShuttingdownWork returns null).
                if (state == ThreadPoolState.shuttingdown)
                    return true;

                if (hasTooManyWorkers()) {
                    //there were too many threads in the pool, so remove this thread and return
                    //false to indicate it should be terminated. If the worker is not removed here,
                    //another worker could also think he should be removed and this means that
                    //too many threads terminate.

                    workers.remove(this);
                    //return false to indicate that the job should not be executed again
                    return false;
                } else {
                    //the pool is not too large, and it it still running, so return true to indicate
                    //that the threadPoolJob should be executed again
                    return true;
                }
            } finally {
                mainLock.unlock();
            }
        }

        /**
         * Checks if there are too many workers (if there are more threads in the threadpool
         * than 'desiredPoolSize'.
         *
         * @return true if there are too many workers, false otherwise.
         */
        private boolean hasTooManyWorkers() {
            return workers.size() > desiredPoolsize;
        }

        /**
         * Interrupts the thread only if isn't executing a threadPoolJob.
         * It doesn't meant that the worker receives an InterruptedException (it depends on the implementation
         * of ThreadPoolJob.getWork())
         *
         * @return true if the thread was idle, false otherwise.
         */
        private boolean interruptIfIdle() {
            //if the running lock is available, it isn't running, so
            //it can be interrupted.
            if (runningLock.tryLock()) {
                try {
                    thread.interrupt();
                    //interrupt was success.
                    return true;
                } finally {
                    runningLock.unlock();
                }
            } else {
                //the lock could not be obtained because the worker is 'active' and not idle
                //and this means that it should not be interrupted.
                return false;
            }
        }

        //todo: code is not pretty (it is vague why the state is compared with running state)
        boolean isThreadPoolShuttingdownNormally() {
            mainLock.lock();
            try {
                return state != ThreadPoolState.running;
            } finally {
                mainLock.unlock();
            }
        }

        public void run() {
            try {
                mainloop();
                if (isThreadPoolShuttingdownNormally())
                    shuttingdownloop();
            } finally {
                workerDone();
            }
        }

        /**
         * This is the loop the workers keep executing when the threadpool is
         * running.
         */
        private void mainloop() {
            //loop that processes all work for shutting down.
            for (; ;) {
                //state is volatile, so no locking required.
                //if the threadpool is shutting down, the worker can end itself.
                if (state != ThreadPoolState.running)
                    break;

                Object work;
                try {
                    //todo: getWork is not protected
                    work = threadPoolJob.getWork();

                    //execute the work that was found
                    if (!executeWork(work)) {
                        //if the execution of the task returned false, the worker can end itself.
                        break;
                    }
                } catch (InterruptedException e) {
                    //The thread can be interrupted, while getting work, by the threadpool.
                    //The interrupt itself can be ignored. If the threadpool wants to shut
                    //down, the mainloop will be ended as soon as the poolthread enters the
                    //mainloop again. If a thread was interrupted otherwise, the interrupt
                    //will be eaten up.
                }

                //if the worker is not wanted anymore, the worker can end itself
                if (!wantedWorkerCheck())
                    break;
            }
        }

        private void shuttingdownloop() {
            assert state == ThreadPoolState.shuttingdown;

            for (; ;) {
                Object work = null;
                try {
                    work = threadPoolJob.getShuttingdownWork();
                    //if null was returned, the worker is completely finished.
                    //and can terminate the loop.
                    if (work == null)
                        break;
                } catch (InterruptedException ex) {
                    //ignore it. If the worker was interrupted while getting work for shutdown,
                    //just keep trying until null is returned.
                } catch (Exception ex) {
                    //todo: needs to be handled
                }

                //if work was retrieved, execute it.
                if (work != null) {
                    executeWork(work);
                }
            }
        }


        /**
         * Lets the Worker execute the Work. The work is executed under a runningLock that marks it as
         * 'running' and prevents it from being seen as idle (and being interrupted while idle).
         * <p/>
         * All exceptions are caught and send to the exceptionHandler. Other throwable's, like Error,
         * or not caught and could potentially damage the ThreadPool internals. The exceptionhandler
         * is also called under the same runningLock as defaultWorkJob.executeWork. Meaning that it also
         * won't be seen as an idle action. If an exceptionhandler throws an exception, this exception
         * is gobbled up.
         *
         * @param work
         */
        private boolean executeWork(Object work) {
            runningLock.lock();
            try {
                return threadPoolJob.executeWork(work);
            } catch (Exception e) {
                handleException(e);
                return true;
            } finally {
                removeInterruptStatus();
                runningLock.unlock();
            }
        }

        private void handleException(Exception e) {
            //the exceptionHandler also is used under the runningLock. The exceptionhandler
            //also is used under a try/catch meaning that an exception handler that throws an
            //exception won't corrupt the threadpool.
            try {
                exceptionHandler.handle(e);
            } catch (Exception ex) {
                //just eat the exception.
                //todo: some sort of logging would not be better? Because eating up
                //an exception could lead to not noticing problems.
            }
        }

        private void removeInterruptStatus() {
            Thread.interrupted();
        }

        /**
         * Does cleanup when a worker terminates. If it is the last worker, it marks the threadpool
         * as completely shutdown (the last one that leaves the building should turn off the lights).
         * <p/>
         * This call locks the mainLock.
         */
        private void workerDone() {
            //this worker is going to terminate.
            mainLock.lock();
            try {
                workers.remove(this);//it could be that the thread already is removed.

                //if the last worker is leaving the building, it should turn off the lights
                if (state != ThreadPoolState.running && workers.isEmpty())
                    updateState(ThreadPoolState.shutdown);
            } finally {
                mainLock.unlock();
            }
        }
    }
}
