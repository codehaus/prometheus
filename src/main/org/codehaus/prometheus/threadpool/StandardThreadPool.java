package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.exceptionhandler.NullExceptionHandler;
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
 * The default implementation of the {@link ThreadPool} interface.
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool implements ThreadPool {

    private final Lock mainLock = new ReentrantLock();
    private final Latch shutdownLatch = new Latch(mainLock);
    private final Set<Worker> workers = new HashSet<Worker>();
    private final ThreadFactory threadFactory;
    private volatile WorkerJob defaultWorkerJob;
    private volatile ThreadPoolState state = ThreadPoolState.unstarted;
    private volatile int desiredPoolsize;
    private volatile ExceptionHandler exceptionHandler = NullExceptionHandler.INSTANCE;

    /**
     * Creates a new StandardThreadPool with a {@link StandardThreadFactory} as ThreadFactory
     * an no {@link WorkerJob}.
     */
    public StandardThreadPool() {
        this(new StandardThreadFactory());
    }

    public StandardThreadPool(int poolsize){
        this();
        setDesiredPoolsize(poolsize);
    }

    /**
     * Creates a new StandardThreadPool with the given ThreadFactory and no
     * {@link WorkerJob}.
     *
     * @param factory the ThreadFactory that is used to fill the pool.
     * @throws NullPointerException if factory is <tt>null</tt>.
     */
    public StandardThreadPool(ThreadFactory factory) {
        this(null,factory);
    }

    public StandardThreadPool(int poolsize, ThreadFactory threadFactory){
        this(null,threadFactory);
        setDesiredPoolsize(poolsize);
    }
    
    /**
     * Creates a new StandardThreadPool with the given {@link ThreadFactory} and
     * workerJob.
     *
     * @param workerJob
     * @param factory   the ThreadFactory that is used to fill the pool.
     * @throws NullPointerException if workerJob or factory is <tt>null</tt>.
     */
    public StandardThreadPool(WorkerJob workerJob, ThreadFactory factory) {
        if (factory == null) throw new NullPointerException();
        this.defaultWorkerJob = workerJob;
        this.threadFactory = factory;
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler handler) {
        if (handler == null) throw new NullPointerException();
        this.exceptionHandler = handler;
    }

    public WorkerJob getDefaultWorkerJob() {
        return defaultWorkerJob;
    }

    public ThreadPoolState getState() {
        return state;
    }

    public void start() {
        mainLock.lock();
        try {
            switch (state) {
                case unstarted:
                    if (defaultWorkerJob == null)
                        throw new IllegalStateException("Can't start, nu workerJob is set");
                    updateState(ThreadPoolState.started);
                    createWorkers(desiredPoolsize);
                    break;
                case started:
                    //ignore call, threadpool already is started
                    return;
                case shuttingdown:
                    throw new IllegalStateException();
                case shutdown:
                    throw new IllegalStateException();
                default:
                    throw new RuntimeException("unhandled state: "+state);
            }
        } finally {
            mainLock.unlock();
        }
    }

    public int getActualPoolSize() {
        mainLock.lock();
        try {
            return workers.size();
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
                case started:
                    int extraThreads = desiredPoolsize - getActualPoolSize();
                    if (extraThreads == 0)
                        return;

                    this.desiredPoolsize = desiredPoolsize;
                    if (extraThreads > 0) {
                        createWorkers(extraThreads);
                    } else {
                        interruptIdleWorkers(-extraThreads);
                    }
                    break;
                case shuttingdown:
                    throw new IllegalStateException();
                case shutdown:
                    throw new IllegalStateException();
                default:
                    throw new RuntimeException("unhandeled state:" + state);
            }
        } finally {
            mainLock.unlock();
        }
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

    private void interruptAllWorkers() {
        for (Worker worker : workers)
            worker.thread.interrupt();
    }

    private void interruptIdleWorkers() {
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
    private void createWorkers(int count) {
        assert count >= 0;
        for (int k = 0; k < count; k++)
            createNewWorker();
    }

    /**
     * Creates and registers a new Worker.
     * <p/>
     * Call only should be made when the main lock is held.
     *
     * @return the created worker.
     */
    private Worker createNewWorker() {
        assert state == ThreadPoolState.started;

        Worker worker = new Worker();
        Thread t = threadFactory.newThread(worker);
        worker.thread = t;
        workers.add(worker);
        t.start();
        return worker;
    }

    public ThreadPoolState shutdownNow() {
        return shutdown(true);
    }

    public ThreadPoolState shutdown() {
        return shutdown(false);
    }

    /**
     * Changes the state this StandardThreadPool is in. All changes to the state should
     * be made through this method.
     * <p/>
     * Call only should be made when the main lock is held.
     *
     * @param newState
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

    private ThreadPoolState shutdown(boolean interruptWorkers) {
        mainLock.lock();
        try {
            switch (state) {
                case unstarted:
                    return updateState(ThreadPoolState.shutdown);
                case started:
                    if (workers.isEmpty()) {
                        return updateState(ThreadPoolState.shutdown);
                    } else {
                        updateState(ThreadPoolState.shuttingdown);
                        if (interruptWorkers) {
                            interruptAllWorkers();
                        } else {
                            interruptIdleWorkers();
                        }
                    }
                    return ThreadPoolState.started;
                case shuttingdown:
                    if (interruptWorkers)
                        interruptAllWorkers();
                    return ThreadPoolState.shuttingdown;
                case shutdown:
                    return ThreadPoolState.shutdown;
                default:
                    throw new RuntimeException("unhandeled state: " + state);
            }
        } finally {
            mainLock.unlock();
        }
    }

    public Lock getStateChangeLock() {
        return mainLock;
    }

    public void awaitShutdown() throws InterruptedException {
        shutdownLatch.await();
    }

    public void tryAwaitShutdown(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        shutdownLatch.tryAwait(timeout, unit);
    }

    public void pauze() {
        mainLock.lock();
        try {
            switch (state) {
                case unstarted:
                    throw new RuntimeException("not implemented");
                case started:
                    throw new RuntimeException("not implemented");
                case shuttingdown:
                case shutdown:
                    throw new IllegalStateException("");
                default:
                    throw new RuntimeException("unhandled state:" + state);
            }
        } finally {
            mainLock.unlock();
        }
    }

    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public void setWorkerJob(WorkerJob defaultJob) {
        if (defaultJob == null) throw new NullPointerException();

        mainLock.lock();
        try {
            if (state != ThreadPoolState.unstarted)
                throw new IllegalStateException();

            this.defaultWorkerJob = defaultJob;
        } finally {
            mainLock.unlock();
        }
    }

    private class Worker implements Runnable {

        private volatile Thread thread;
        private final Lock runningLock = new ReentrantLock();

        /**
         * Returns true if the defaultWorkerJob should be run again, false otherwise.
         *
         * @return true if the defaultWorkerJob should be run again, false otherwise
         */
        private boolean runAgain() {
            //this method can't be called if the threadpool is unstarted, of shutdown.
            assert state == ThreadPoolState.started || state == ThreadPoolState.shuttingdown;

            mainLock.lock();
            try {
                if (state == ThreadPoolState.shuttingdown)
                    return false;

                if (hasTooManyWorkers()) {
                    //there were too many threads in the pool, so remove this thread and return
                    //false to indicate it should be terminated. If the worker is not removed,
                    //another worker could also think he should be removed.

                    workers.remove(this);
                    return false;
                } else {
                    //the pool is not too large, and it it still running, so return true
                    //to run 
                    return true;
                }
            } finally {
                mainLock.unlock();
            }
        }

        private boolean hasTooManyWorkers() {
            return workers.size() > desiredPoolsize;
        }

        /**
         * Interrupts the thread only if isn't executing a defaultWorkerJob.
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
                //interrupt was failure.
                return false;
            }
        }

        public void run() {
            try {
                while (runAgain()) {
                    //get the task
                    Object task = null;
                    try {
                        //while getting the defaultWorkerJob, a thread can be interrupted
                        //this means that is can interrupt when it is waiting
                        //for something to do.
                        task = defaultWorkerJob.getTask();
                    } catch (InterruptedException ex) {
                        //do nothing
                        //     System.out.println("interrupted while getting task");
                    } catch (Exception ex) {
                        exceptionHandler.handle(ex);
                    }

                    //execute the task if one is retrieved.
                    if (task != null) {
                        try {
                            runningLock.lock();
                            try {
                                defaultWorkerJob.executeTask(task);
                            } finally {
                                runningLock.unlock();
                            }
                        } catch (Exception e) {
                            exceptionHandler.handle(e);
                        } finally {
                            //remove the interrupt flag, if the threadpool is shutting down (and possibly
                            //interrupted the thread) the loop will shutdown when it does the runAgain.
                            Thread.interrupted();
                        }
                    }
                }
            } finally {
                workerDone();
            }
        }

        private void workerDone() {
            //this worker is going to terminate.
            mainLock.lock();
            try {
                workers.remove(this);//it could be that the thread already is removed.
                if (state == ThreadPoolState.shuttingdown && workers.isEmpty()) {
                    //it is the last thread, and the threadpool is shutting down
                    updateState(ThreadPoolState.shutdown);
                    shutdownLatch.open();
                }
            } finally {
                mainLock.unlock();
            }
        }
    }
}
