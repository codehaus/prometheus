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
 */
public class StandardThreadPool implements ThreadPool {

    private final Lock mainLock = new ReentrantLock();
    private final Latch shutdownLatch = new JucLatch(mainLock);
    private final Set<Worker> workers = new HashSet<Worker>();
    private final ThreadFactory threadFactory;
    private volatile WorkerJob workerJob;
    private volatile ThreadPoolState state = ThreadPoolState.unstarted;
    private volatile int desiredPoolsize;
    private volatile ExceptionHandler exceptionHandler = NullExceptionHandler.INSTANCE;
    private boolean c;

    /**
     * Creates a new StandardThreadPool with a {@link StandardThreadFactory} as ThreadFactory
     * an no {@link WorkerJob} and zero threads in the threadpool.
     */
    public StandardThreadPool() {
        this(new StandardThreadFactory());
    }

    /**
     * Creates a new StandardThreadPool with a {@link StandardThreadFactory}, no {@link WorkerJob}
     * and the given number of threads in the threadpool.
     *
     * @param poolsize the number of threads in the threadpool.
     * @throws IllegalArgumentException if poolsize is smaller than 0.
     */
    public StandardThreadPool(int poolsize) {
        this();
        setDesiredPoolsize(poolsize);
    }

    /**
     * Creates a new StandardThreadPool with the given ThreadFactory and no
     * {@link WorkerJob} and no threads in the threadpool.
     *
     * @param factory the ThreadFactory that is used to fill the pool.
     * @throws NullPointerException if factory is <tt>null</tt>.
     */
    public StandardThreadPool(ThreadFactory factory) {
        this(0, null, factory);
    }

    /**
     * Creates a new StandardThreadPool with the given poolsize and ThreadFactory.
     * The WorkerJob needs to be set before this StandardThreadPool is running.
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
     * workerJob and no threads in the threadpool.
     *
     * @param workerJob the job that should be executed. The value is allowed to be null, but needs to be
     *                  set before running.
     * @param factory   the ThreadFactory that is used to fill the pool.
     * @throws NullPointerException if workerJob or factory is <tt>null</tt>.
     */
    public StandardThreadPool(WorkerJob workerJob, ThreadFactory factory) {
        this(0, workerJob, factory);
    }

    /**
     * Creates a new StandardThreadPool with the given poolsize, ThreadFactory and workerjob.
     *
     * @param poolsize  the initial size of the threadpool.
     * @param workerJob the job that should be executed. The value is allowed to be <tt>null</tt>, but
     *                  needs to be set before running.
     * @param factory   the ThreadFactory that is used to fill the pool.
     * @throws IllegalArgumentException if poolsize smaller than zero.
     * @throws NullPointerException     if factory is null.
     */
    public StandardThreadPool(int poolsize, WorkerJob workerJob, ThreadFactory factory) {
        if (poolsize < 0) throw new IllegalArgumentException();
        if (factory == null) throw new NullPointerException();
        this.workerJob = workerJob;
        this.threadFactory = factory;
        this.desiredPoolsize = poolsize;
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler handler) {
        this.exceptionHandler = handler == null ? NullExceptionHandler.INSTANCE : handler;
    }

    public WorkerJob getWorkerJob() {
        return workerJob;
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
                case shuttingdown://fall through
                case forcedshuttingdown://fall through
                case shutdown:
                    throw new IllegalStateException();//todo
                default:
                    throw new RuntimeException("unhandled state: " + state);
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * Makes sure that the the workerJob is available. If it isn't available, an
     * IllegalStateException is thrown. This call only should be made when the
     * mainLock is hold.
     */
    private void ensureWorkerJobAvailable() {
        if (workerJob == null)
            throw new IllegalStateException("Can't spawned_start, nu workerJob is set");
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
                case running:
                    this.desiredPoolsize = desiredPoolsize;

                    int extraThreads = desiredPoolsize - getActualPoolSize();
                    if (extraThreads == 0)
                        return;

                    if (extraThreads > 0)
                        createNewWorkers(extraThreads);
                    else
                        interruptIdleWorkers(-extraThreads);
                    break;
                case shuttingdown://fall through
                case forcedshuttingdown:
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
     * Creates and registers a new Worker.
     * <p/>
     * Call only should be made when the main lock is held.
     *
     * @return the created worker.
     */
    private Worker createNewWorker() {
        assert state == ThreadPoolState.running;

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
     * @param forced
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
                        return updateState(ThreadPoolState.shutdown);
                    } else {
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

    public Lock getStateChangeLock() {
        return mainLock;
    }

    public void awaitShutdown() throws InterruptedException {
        shutdownLatch.await();
    }

    public void tryAwaitShutdown(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        shutdownLatch.tryAwait(timeout, unit);
    }

    public boolean isShutdownWhenPoolDriesUp(){
        return c;
    }

    public void setShutdownWhenPoolDriesUp(boolean c){
        this.c = c;
    }

    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public void setWorkerJob(WorkerJob workerJob) {
        if (workerJob == null) throw new NullPointerException();

        mainLock.lock();
        try {
            if (state != ThreadPoolState.unstarted)
                throw new IllegalStateException("workerJob can only be set on an unstarted ThreadPool");

            this.workerJob = workerJob;
        } finally {
            mainLock.unlock();
        }
    }

    enum workerstate {
        running
    }

    //todo: idea create workerstate?
    private class Worker implements Runnable {
        private final Lock runningLock = new ReentrantLock();
        //the thread that executes this runnable.
        private volatile Thread thread;

        /**
         * Returns true if the workerJob should be run again, false otherwise.
         *
         * @return true if the workerJob should be run again, false otherwise
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
                    return false;
                } else {
                    //the pool is not too large, and it it still running, so return true
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
         * Interrupts the thread only if isn't executing a workerJob.
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
                //if the threadpool is shutting down, the loop should end
                if (state != ThreadPoolState.running)
                    break;

                Object work = null;
                try {
                    //todo: getWork is not protected
                    work = workerJob.getWork();
                    if (work == null){
                        //the worker got the signal that it should terminate itself.
                        //so break the main loop.
                        //todo: when a thread stops this loop because his it should
                        //terminate, it also should deal with the desiredPoolSize. If
                        //not is dealt with this issue, the actual poolsize could be
                        //lower than the desiredPoolSize for an indertemined amount
                        //of time. But is lowering the desiredPoolSize the appropriate
                        //behavior?
                        break;
                    }else{
                        //work was found, so execute it. This is the logic that
                        //is executed in most cases.
                        runWork(work);
                    }
                } catch (InterruptedException e) {
                    //The thread can be interrupted by the threadpool when it want
                    //an idle thread to wake up very hard.
                    //
                    //ignore it. if the worker is going to shutdown
                    //it will step the next time it enters this loop.
                }

                if (!wantedWorkerCheck())
                    break;
            }
        }

        private void shuttingdownloop() {
            assert state == ThreadPoolState.shuttingdown;

            for (; ;) {
                Object work = null;
                try {
                    work = workerJob.getShuttingdownWork();
                    //if null was returned, the worker is completely finished.
                    //and can terminate the loop.
                    if (work == null)
                        break;
                } catch (InterruptedException ex) {
                    //ignore it. If the worker was interrupted while getting work for shutdown,
                    //just keep trying untill nul is returned.
                } catch (Exception ex) {
                    //todo: needs to be handled
                }

                //if work was retrieved, execute it.
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
                workerJob.runWork(work);
            } catch (Exception e) {
                handleException(e);
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

        //problem with shutdown. If a thread is removed because the pool shrinks, and then
        //the shutdown is called, there could be multiple threads that are unwanted, and
        //when such an unwanted thread reaches the end of its life (workerDone) multiple
        //updateStates could occur.

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

                //if it is the last thread and
                if (state != ThreadPoolState.running && workers.isEmpty()) {
                    updateState(ThreadPoolState.shutdown);
                }
            } finally {
                mainLock.unlock();
            }
        }
    }
}
