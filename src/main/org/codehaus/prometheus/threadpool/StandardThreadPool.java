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
 * <p/>
 * Thread Terminate themself. This beats the whole purpose of
 * the return value in {@link org.codehaus.prometheus.threadpool.WorkerJob#runWork(Object)} method.
 * <p/>
 * The problem at the moment is shutting down. After the shutdown is called, which guarantees are made for the
 * worker threads. If the worker thread is getting his work, how can this be 'broken'? We don't want the task
 * to wait for something to occur that can't occur (deadlock). Eg: blocking executor doesn't accept new
 * jobs to be placed as soon as it shuts down, but if a worker happens to do a block on the workqueue,
 * it will never be waked up because nothing can place a new task. So who has the responsibility of shutting down
 * threads? Should it be the enclosing structure? It could work with poison messages.
 * <p/>
 * ANother problem:
 * if a thread retrieves false when doing a runJob, it will terminate itself. This means that the
 * actual poolsize can be smaller than the desired poolsize after termination.
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

    public StandardThreadPool(int poolsize) {
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
        this(null, factory);
    }

    public StandardThreadPool(int poolsize, ThreadFactory threadFactory) {
        this(null, threadFactory);
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
                    throw new RuntimeException("unhandled state: " + state);
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
                    throw new IllegalStateException("Can't change the poolsize because this threadpool is shutting down");
                case shutdown:
                    throw new IllegalStateException("Can't change the poolsize because this threadpool is shut down");
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
        private boolean wantedWorkerCheck() {
            //this method can't be called if the threadpool is unstarted, of shutdown.
            assert state == ThreadPoolState.started || state == ThreadPoolState.shuttingdown;

            mainLock.lock();
            try {
                //if the threadpool is shutting down, we don't want workers to be terminated.
                //Workers are going to terminate themselves when there is nothing more to do
                //for them (so when getWorkWhileShuttingdown returns null).
                if (state == ThreadPoolState.shuttingdown)
                    return true;

                if (hasTooManyWorkers()) {
                    //there were too many threads in the pool, so remove this thread and return
                    //false to indicate it should be terminated. If the worker is not removed here,
                    //another worker could also think he should be removed and this means that
                    //too many threads terminate.

                    workers.remove(this);
                    return false;
                } else {
                    //the pool is not too large, and it it still running, so return true
                    //to runWork
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
         * Interrupts the thread only if isn't executing a defaultWorkerJob.
         * It doesn't meant that the worker receives an InterruptedException (it depends on the implementation
         * of WorkerJob.getWork())
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
                //the lock could not be obtained because the worker is 'active'.
                //and this means that it is not interrupted.
                return false;
            }
        }

        boolean isShuttingDown() {
            mainLock.lock();
            try {
                return ThreadPoolState.shuttingdown == state;
            } finally {
                mainLock.unlock();
            }
        }

        public void run() {
            try {
                mainloop();
                if (isShuttingDown()) {
                    shuttingdownloop();
                }
            } finally {
                workerDone();
            }
        }

        private void mainloop() {
            //loop that processes all work for shutting down.
            for (; ;) {
                if (isShuttingDown())
                    break;

                Object work = null;
                try {
                    work = defaultWorkerJob.getWork();
                } catch (InterruptedException ex) {
                    //ignore it
                } catch (Exception ex) {
                    //todo: needs to be handled
                }

                if (work != null)
                    runWork(work);

                if (!wantedWorkerCheck())
                    break;
            }
        }

        private void shuttingdownloop() {
            for (; ;) {
                Object work = null;
                try {
                    work = defaultWorkerJob.getWorkWhileShuttingdown();
                    //if null was returned, the worker is completely finished.
                    //and can terminate the loop.
                    if (work == null)
                        break;
                } catch (InterruptedException ex) {
                    //ignore it. If the worker was interrupted while getting work for shutdown,
                    //just keep trying untill null is returned.
                } catch (Exception ex) {
                    //todo: needs to be handled
                }

                //if work was retrieved, execute it. If null was returned we would not come here.
                if (work != null)
                    runWork(work);
            }
        }


        /**
         * Lets the Worker execute the Work. The work is executed under a runningLock that marks it as
         * 'running' and prevents it from being seen as idle (and being interrupted while idle).
         * <p/>
         * All exceptions are caught and send to the exceptionHandler. Other throwable's, like Error,
         * or not caught and could potentially damage the ThreadPool internals. The exceptionhandler
         * is also called under the same runningLock as defaultWorkJob.runWork. Meaning that it also
         * won't be seen as an idle action. If an exceptionhandler throws an exception, this exception
         * is gobbled up.
         *
         * @param work
         */
        private void runWork(Object work) {
            runningLock.lock();
            try {
                defaultWorkerJob.runWork(work);
            } catch (Exception e) {
                //the exceptionHandler also is used under the runningLock. The exceptionhandler
                //also is used under a try/catch meaning that an exception handler that throws an
                //exception won't corrupt the threadpool.
                try {
                    exceptionHandler.handle(e);
                } catch (Exception ex) {
                    //just eat the exception.
                }
            } finally {
                //remove the interrupt flag, if the threadpool is shutting down (and possibly
                //interrupted the thread)
                Thread.interrupted();
                runningLock.unlock();
            }
        }

        /**
         * Does cleanup when a worker terminates. If it is the last worker, it marks the threadpool
         * as completely shutdown.
         */
        private void workerDone() {
            //this worker is going to terminate.
            mainLock.lock();
            try {
                workers.remove(this);//it could be that the thread already is removed.
                if (state == ThreadPoolState.shuttingdown && workers.isEmpty()) {
                    updateState(ThreadPoolState.shutdown);
                }
            } finally {
                mainLock.unlock();
            }
        }
    }
}
