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
 * Op het moment dat een worker klaar is met zijn taak, dus als de taak is afgelopen, dan
 * moet de thread gepooled worden?
 * <p/>
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
    private final Set<InitialSpawn> initialSpawns = new HashSet<InitialSpawn>();

    private volatile ThreadFactory threadFactory;
    //is always protected by the mainLock
    private ThreadPoolState state = ThreadPoolState.unstarted;
    private volatile ExceptionHandler exceptionHandler = NoOpExceptionHandler.INSTANCE;

    /**
     * Creates a new StandardThreadPool with a {@link StandardThreadFactory} as ThreadFactory.
     */
    public StandardThreadPool() {
        this(createDefaultThreadFactory());
    }

    /**
     * Creates a new StandardThreadPool with the given ThreadFactory.
     *
     * @param threadFactory the ThreadFactory that is used to fill the pool.
     * @throws NullPointerException if threadFactory is <tt>null</tt>.
     */
    public StandardThreadPool(ThreadFactory threadFactory) {
        if (threadFactory == null) throw new NullPointerException();
        this.threadFactory = threadFactory;
    }

    public Lock getStateChangeLock() {
        return mainLock;
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler handler) {
        if (handler == null) throw new NullPointerException();
        this.exceptionHandler = handler;
    }

    public ThreadPoolState getState() {
        mainLock.lock();
        try {
            return state;
        } finally {
            mainLock.unlock();
        }
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

    /**
     * Sets the ThreadFacory this StandardThreadPool uses for the creation of threads.
     *
     * @param threadFactory
     * @throws NullPointerException if threadFactory is null.
     */
    public void setThreadFactory(ThreadFactory threadFactory) {
        if (threadFactory == null) throw new NullPointerException();
        this.threadFactory = threadFactory;
    }

    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public void spawn(ThreadPoolJob job, int count) {
        if (job == null) throw new NullPointerException();
        if (count < 0) throw new IllegalArgumentException();

        mainLock.lock();
        try {
            switch (state) {
                case unstarted:
                    if(count == 0)
                        return;

                    start();
                    //fall through
                case running:
                case shuttingdownnormally:
                case shuttingdownforced:
                    for (int k = 0; k < count; k++)
                        createNewWorker(job);
                    break;
                case shutdown:
                    throw new IllegalStateException("Can't spawn, threadpool is shutdown");
                default:
                    throw new RuntimeException("unhandeled state:" + state);
            }
        } finally {
            mainLock.unlock();
        }
    }

    public void spawn(ThreadPoolJob job) {
        spawn(job, 1);
    }

    public void spawnWithoutStarting(ThreadPoolJob job, int threadcount) {
        if (job == null) throw new NullPointerException();
        if (threadcount < 0) throw new IllegalArgumentException();

        mainLock.lock();
        try {
            switch (state) {
                case unstarted:
                    if (threadcount > 0)
                        initialSpawns.add(new InitialSpawn(threadcount, job));
                    break;
                case running:
                    spawn(job, threadcount);
                    break;
                default:
                    throw new IllegalStateException("Can't set initial poolsize, threadpool is not unstarted");
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
                    updateState(ThreadPoolState.running);
                    startInitialSpawnsAndCleanup();
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

    private void startInitialSpawnsAndCleanup() {
        for (InitialSpawn initialSpawn : initialSpawns)
            spawn(initialSpawn.job, initialSpawn.threadcount);

        //we can remove the initial spawns to prevent memory leaks.
        initialSpawns.clear();
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
     * Creates and registers a new Worker. The created worker is added to the
     * set of workers. Actual reference to the Worker is not required for the moment,
     * hence the void return value.
     * <p/>
     * Call only should be made when the main lock is held.
     */
    private void createNewWorker(ThreadPoolJob job) {
        assert state == ThreadPoolState.running;

        Worker worker = new Worker(job);
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
                        //if there are no workers, the threadpool the threadpool is completely shut down
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
        //the thread belongs-to/executes this Worker.
        private volatile Thread thread;
        private final ThreadPoolJob job;
        private final Lock isExecutingWorkLock = new ReentrantLock();

        public Worker(ThreadPoolJob job) {
            this.job = job;
        }

        public void run() {
            try {
                workLoop();
            } finally {
                workerCompleted();
            }
        }

        /**
         * This is the loop the workers keep executing when the threadpool is
         * running.
         */
        private void workLoop() {
            while (true) {
                try {
                    //todo:
                    //the job.takeWork definition has been changed, a null is now
                    //allowed to be returned. So what does it mean? 
                    Object work = job.takeWork();

                    //if the execution of the work returned false, the worker can end itself.
                    //todo: at the moment the executeWork needs to be able to deal with null as work
                    if (!executeWork(work))
                        break;
                } catch (InterruptedException e) {
                    //The thread can be interrupted, while getting work by the threadpool.
                } catch (RuntimeException ex) {
                    //todo : handle exception for taking work
                    ex.printStackTrace();
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
        private boolean executeWork(Object work) {
            isExecutingWorkLock.lock();
            try {
                return job.executeWork(work);
            } catch (Exception ex) {
                handleExceptionForExecuteWork(ex);
                //todo: this return value should be part of the exception handling?
                return true;
            } finally {
                removeInterruptStatus();
                isExecutingWorkLock.unlock();
            }
        }

        /**
         * Interrupts the thread only if isn't executing a threadPoolJob. It doesn't meant that the worker actually
         * is interrupted (it depends on the implementation of {@link ThreadPoolJob#takeWork()}.
         * <p/>
         * This call is not made by the worker Thread (unless we the worker calls back on the threadpool)
         * but by the thread that callls an uperation like shutdown
         *
         * @return true if the thread was interrupted, false otherwise.
         */
        private boolean interruptIfIdle() {
            if (!isExecutingWorkLock.tryLock()) {
                //the lock could not be obtained because the worker is executing work and not idle
                //and this means that it should not be interrupted.  It returns false to indicate
                //that the interrupt didn't happen.
                return false;
            }

            //if the isExecutingWorkLock is available, the worker isn't running it can be interrupted.
            try {
                thread.interrupt();
                //interrupt was success.
                return true;
            } finally {
                isExecutingWorkLock.unlock();
            }
        }

        /**
         * The ExceptionHandler is executed under a try/catch meaning that an exception handler that throws an
         * exception won't corrupt the threadpool. If this happens, the Stacktrace is print in the System.err
         *
         * @param exception the Exception to handle.
         */
        private void handleExceptionForExecuteWork(Exception exception) {
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
                workers.remove(this);

                //if it was the last worker, it should turn of the lights.
                if (workers.isEmpty() && state.isShuttingdownState())
                    updateState(ThreadPoolState.shutdown);
            } finally {
                mainLock.unlock();
            }
        }
    }

    class InitialSpawn {
        final int threadcount;
        final ThreadPoolJob job;

        InitialSpawn(int threadcount, ThreadPoolJob job) {
            this.threadcount = threadcount;
            this.job = job;
        }
    }
}