package org.codehaus.prometheus.util;

/**
 * A {@link Runnable} that has a priority and can be used in a Executor/BlockingExecutor
 * in combination with a PriorityBlockingQueue.
 *
 */
public class PriorityRunnable implements Runnable,Comparable<PriorityRunnable> {
    private final long priority;

    public PriorityRunnable(long priority){
        this.priority = priority;
    }

    public long getPriority() {
        return priority;
    }

    public int compareTo(PriorityRunnable o) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void run() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
