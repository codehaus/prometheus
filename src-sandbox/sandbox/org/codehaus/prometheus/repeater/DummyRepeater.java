package org.codehaus.prometheus.repeater;

import org.codehaus.prometheus.exceptionhandler.ExceptionHandler;
import org.codehaus.prometheus.lendablereference.LendableReference;
import org.codehaus.prometheus.threadpool.ThreadPool;
import org.codehaus.prometheus.threadpool.ThreadPoolState;
import org.codehaus.prometheus.threadpool.WorkerJob;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class DummyRepeater implements RepeaterService {

    private final LendableReference<Repeatable> lendableRef;
    private final ThreadPool threadPool;

    public DummyRepeater(ThreadPool threadPool, LendableReference<Repeatable> lendableRef) {
        this.threadPool = threadPool;
        this.lendableRef = lendableRef;
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void repeat(Repeatable task) throws InterruptedException {
        if (threadPool.getState() != ThreadPoolState.started)
            throw new RejectedExecutionException();

        //it cnould be that the repeater just has begon shutting down,
        //or completely has shut down. It is up to the task to figure out
        //if it is executed.
        lendableRef.put(task);
    }


    public boolean tryRepeat(Repeatable task) {
        throw new RuntimeException();
    }

    public void tryRepeat(Repeatable task, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (threadPool.getState() != ThreadPoolState.started)
            throw new RejectedExecutionException();

        //it could be that this method is called even though the threadpool is shutting down, or shut down.
        //this means that a task is placed, but not executed.
        lendableRef.tryPut(task, timeout, unit);
    }

    private class WorkerTaskImpl implements WorkerJob<Repeatable> {

        public Repeatable getTask() throws InterruptedException {
            return lendableRef.take();
        }

        public boolean executeTask(Repeatable task) throws Exception {
            boolean again = true;
            try {
                again = task.execute();
                return true;
            } finally {
                if (again)
                    lendableRef.takeback(task);
                else
                    lendableRef.takebackAndReset(task);
            }
        }
    }
}
