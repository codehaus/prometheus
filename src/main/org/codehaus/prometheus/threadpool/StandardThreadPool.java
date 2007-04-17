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
 * problem:
 * the desiredpoolsize can be larger than the actual poolsize if threads are terminating.
 * <p/>
 * guarantee:
 * after the shutdown is called (same goes for the shutdownNow) the size of the pool is not
 * going to increase.
 * <p/>
 * guarantee:
 * when the threadpool is shutting down, when the last thread exist, it marks the threadpool
 * as shut down.
 * <p/>
 * idea:
 * when the threadpool is shutting down, threads won't terminate themselves when they see
 * that the desired poolsize is smaller than the actual poolsize.
 * <p/>
 * deadlock:
 * when there the threadpool is shutting down, it doesn't allow new threads to be created.
 * when there is outstanding work, and there are no threads to execute it, it could lead
 * to a deadlock because new workers can't be created.
 * <p/>
 * <p/>
 * The ThreadPoolRepeater should keep running when it is shutting down.
 */
public class StandardThreadPool implements ThreadPool {

    private final Lock mainLock = new ReentrantLock();
    private final Latch shutdownLatch = new Latch(mainLock);
    private final Set<Worker> workers = new HashSet<Worker>();
    private final ThreadFactory threadFactory;
    private volatile WorkerJob defaultTask;
    private volatile ThreadPoolState state = ThreadPoolState.unstarted;
    private volatile int desiredPoolsize;
    private volatile ExceptionHandler exceptionHandler = NullExceptionHandler.INSTANCE;

    public StandardThreadPool() {
        this(new StandardThreadFactory());
    }

    public StandardThreadPool(ThreadFactory factory) {
        if (factory == null) throw new NullPointerException();
        this.threadFactory = factory;
    }

    public StandardThreadPool(WorkerJob task, ThreadFactory factory) {
        if (factory == null) throw new NullPointerException();
        this.defaultTask = task;
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
        return defaultTask;
    }

    public ThreadPoolState getState() {
        return state;
    }

    public void start() {
        mainLock.lock();
        try {
            switch (state) {
                case unstarted:
                    if (defaultTask == null)
                        throw new IllegalStateException("nu default WorkerJob is set");
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
                    throw new RuntimeException();
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
                    if (extraThreads > 0){
                        createWorkers(extraThreads);
                    }else{
                        interruptIdleWorkers(-extraThreads);
                    }
                    //workers are going to do the removing themselves if this is required.
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
     *
     * Call only should be made when main lock is hold.
     *
     * @param count the number of idle workers to interrupt
     */
    private void interruptIdleWorkers(int count){
        int interrupted = 0;
        for(Worker worker: workers){
            if(worker.interruptIfIdle())
                interrupted++;
            //if the expected number of workers are interrupted, this call can return.
            if(interrupted == count)
                return;
        }
    }

    private void createWorkers(int count) {
        for (int k = 0; k < count; k++)
            createNewWorker();
    }

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
                        //beter nadenken hier.
                        if (interruptWorkers) {
                            interruptAllWorkers();
                        } else {
                            for (Worker worker : workers)
                                worker.interruptIfIdle();
                        }
                    }
                    return ThreadPoolState.started;
                case shuttingdown:
                    if(interruptWorkers)
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

    private void interruptAllWorkers() {
        for (Worker t : workers)
            t.thread.interrupt();
    }

    public void interruptIdleWorkers() {
        mainLock.lock();
        try {
            for (Worker worker : workers)
                worker.interruptIfIdle();
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

    public void setDefaultWorkerJob(WorkerJob defaultJob) {
        if (defaultJob == null) throw new NullPointerException();

        mainLock.lock();
        try {
            if (state != ThreadPoolState.unstarted)
                throw new IllegalStateException();

            this.defaultTask = defaultJob;
        } finally {
            mainLock.unlock();
        }
    }

    private class Worker implements Runnable {

        private volatile Thread thread;
        private final Lock runningLock = new ReentrantLock();

        /**
         * Returns true if the defaultTask should be run again, false otherwise.
         *
         * @return true if the defaultTask should be run again, false otherwise
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
         * Interrupts the thread only if isn't executing a defaultTask.
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
            }else{
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
                        //while getting the defaultTask, a thread can be interrupted
                        //this means that is can interrupt when it is waiting
                        //for something to do.
                        task = defaultTask.getTask();
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
                                defaultTask.executeTask(task);
                            } finally {
                                runningLock.unlock();
                            }
                        } catch (Exception e) {                            
                            exceptionHandler.handle(e);
                        } finally {
                            //remove the interrupt flag, if the threadpool is shutting down (and possibly
                            //interrupted the thread) the loop will stop when it does the runAgain.
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
