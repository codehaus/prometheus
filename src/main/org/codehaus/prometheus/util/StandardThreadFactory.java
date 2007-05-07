/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * A customizable implementation of the {@link java.util.concurrent.ThreadFactory}.
 * <p/>
 * <p/>
 * todo: daemon threads.
 * <p/>
 * If the maximum priority of the threadgroup is changed after this StandardThreadFactory is
 * constructed, then this will be ignored by the StandardThreadFactory. So it could be that a
 * StandardThreadFactory has a higher priority than the threadgroup allowed. What will happen at
 * construction?
 *
 * @author Peter Veentjer.
 */
public class StandardThreadFactory implements ThreadFactory {

    private static final AtomicInteger poolNumber = new AtomicInteger(1);

    private final ThreadGroup threadGroup;
    private final AtomicInteger _threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final boolean daemon;
    private volatile int priority;

    /**
     * Constructs a new StandardThreadFactory with a Thread.NORM_PRIORITY as priority and a newly
     * created ThreadGroup.
     */
    public StandardThreadFactory() {
        this(Thread.NORM_PRIORITY, createName());
    }

    private static String createName() {
        return Integer.toString(poolNumber.getAndIncrement());
    }

    /**
     * Constructs a new StandardThreadFactory with a Thread.NORM_PRIORITY as priority and with a
     * newly created ThreadGroup with the given groupName.
     *
     * @param groupName the name of the ThreadGroup (is allowed to be null).
     */
    public StandardThreadFactory(String groupName) {
        this(Thread.NORM_PRIORITY, groupName);
    }

    public StandardThreadFactory(int priority) {
        this(priority, createName());
    }

    /**
     * Constructs a new StandardThreadFactory with the given priority and with a newly created
     * ThreadGroup with the given groupname.
     *
     * @param priority  the priority of the threads this StandardThreadFactory is going to create.
     * @param groupName the name of the ThreadGroup (is allowed to be null).
     * @throws IllegalArgumentException if priority is not a valid priority.
     */
    public StandardThreadFactory(int priority, String groupName) {
        this(priority, new ThreadGroup(Thread.currentThread().getThreadGroup(), groupName), false);
    }

    //todo: unit testen.
    public StandardThreadFactory(int priority, ThreadGroup threadGroup) {
        this(priority, threadGroup, false);
    }

    /**
     * Constructs a new StandardThreadFactory with the given priority and threadgroup.
     *
     * @param priority    the priority of the threads this StandardThreadFactory is going to create.
     * @param threadGroup the threadgroup the thread is part of
     * @param daemon      if the thread should be a daemon.
     * @throws IllegalArgumentException if the priority is not valid.
     * @throws NullPointerException     if threadGroup is null.
     */
    public StandardThreadFactory(int priority, ThreadGroup threadGroup, boolean daemon) {
        if (threadGroup == null) throw new NullPointerException();

        ensureValidPriority(priority, threadGroup);

        this.priority = priority;
        this.threadGroup = threadGroup;
        this.daemon = daemon;
        this.namePrefix = "pool-" + threadGroup.getName() + "-thread-";
    }

    private void ensureValidPriority(int priority, ThreadGroup threadGroup) {
        if (priority < Thread.MIN_PRIORITY) {
            throw new IllegalArgumentException("priority can`t be smaller than: " +
                    Thread.MIN_PRIORITY + ", priority was: " + priority);
        }

        if (priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException("priority can`t be greater than: " +
                    Thread.MAX_PRIORITY + ", priority was: " + priority);
        }

        if (priority > threadGroup.getMaxPriority()) {
            throw new IllegalArgumentException(
                    "priority can`t be greater than threadGroup.maxPriority: " +
                            threadGroup.getMaxPriority() + ", priority was: " + priority);
        }
    }

    /**
     * Returns true if this StandardThreadFactory is producing deamon threads, false
     * otherwise.
     *
     * @return true if this StandardThreadFactory is producing deamon threads, false
     *         otherwise.
     */
    public boolean isProducingDaemons() {
        return daemon;
    }

    /**
     * Returns the ThreadGroup of the created Threads.
     *
     * @return the ThreadGroup of the created Threads.
     */
    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    /**
     * Returns the priority of created Threads. This is a value ranging from
     * Thread.MIN_PRIORITY and Thread.MAX_PRIORITY.
     *
     * @return the priortity of created Threads.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the priority of the threads. This will only effect newly created Threads. A value must
     * be set ranging from Thread.MIN_PRIORITY and Thread.MAX_PRIORITY.
     *
     * @param priority the new priority.
     * @throws IllegalArgumentException if priority is smaller than {@link Thread#MIN_PRIORITY} or
     *                                  larger than {@link Thread#MAX_PRIORITY} or larger than the
     *                                  maximum priority of the ThreadGroup.
     */
    public void setPriority(int priority) {
        ensureValidPriority(priority, getThreadGroup());
        this.priority = priority;
    }

    public Thread newThread(Runnable runnable) {
        if (runnable == null) throw new NullPointerException();

        String threadName = namePrefix + _threadNumber.getAndIncrement();
        Thread thread = new Thread(threadGroup, runnable, threadName);
        thread.setDaemon(daemon);
        thread.setPriority(priority);
        return thread;
    }
}
