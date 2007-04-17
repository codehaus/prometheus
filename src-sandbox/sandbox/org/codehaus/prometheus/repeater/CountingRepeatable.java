package org.codehaus.prometheus.repeater;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link Repeatable} that executes a {@link Runnable} the given amount of times.
 *
 * @author Peter Veentjer.
 */
public class CountingRepeatable implements Repeatable, Runnable {

    public final Runnable task;
    public final AtomicLong count = new AtomicLong();

    /**
     * Creates a new CountingRepeatable with the given count.
     *
     * @param count the number of times to execute the task
     * @throws NullPointerException if mainLock is null.
     * @throws IllegalArgumentException if count < 0
     */
    public CountingRepeatable(int count){
        if(count<0)throw new IllegalArgumentException();
        this.count.set(count);
        this.task = null;
    }

    /**
     * Creates a new CountingRepeatable
     *
     * @param task the task to repeat
     * @param count the number of times to repeat the execution of the task
     * @throws NullPointerException if task is null
     * @throws IllegalArgumentException if count < 0
     */
    public CountingRepeatable(Runnable task, int count){
        if(task == null)throw new NullPointerException();
        if(count<0)throw new IllegalArgumentException();
        this.task = task;
        this.count.set(count);
    }


    /**
     * Sets the count.
     *
     * @param count
     * @throws IllegalArgumentException if cound smaller than 0.
     */
    public final void setCount(int count) {
        if (count < 0) throw new IllegalArgumentException();
        this.count.set(count);
    }

    /**
     * Returns the current count. This value could be stale as soon as it is returned.
     *
     * @return the current count
     */
    public final long getCount() {
        return count.longValue();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    public final boolean execute() {
        long count = this.count.getAndDecrement();
        if(count<=0)
            return false;
        
        run();
        return true;
    }

    public void run() {
        if (task == null) throw new IllegalStateException("this method should be overridden if no task is provided");
        task.run();
    }
}
