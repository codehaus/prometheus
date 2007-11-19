/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.concurrenttesting;

import static junit.framework.Assert.*;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.assertAlive;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.assertNotAlive;
import org.codehaus.prometheus.util.StandardThreadFactory;

import static java.util.Collections.synchronizedList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * A {@link ThreadFactory} that decorates a target ThreadFactory and registers all threads
 * that were created. It is useful when you want to keep track of the Threads that are
 * created by the target ThreadFactory.
 * <p/>
 * An important reminder: a thread that is not started, also is not alive. This threadfactory
 * doesn't make a distinction between terminated and non started threads. 
 *
 * todo:
 * because the tracing threadfactory only tracks threads, and not testthreads,
 * a lot of useful info is hard to test. For example the exception status of the threads.
 *
 * @author Peter Veentjer.
 */
public class TracingThreadFactory implements ThreadFactory {
    private final ThreadFactory targetFactory;
    private final List<Thread> threadList = synchronizedList(new LinkedList<Thread>());

    /**
     * Creates a TracingThreadFactory with a {@link StandardThreadFactory}.
     */
    public TracingThreadFactory() {
        this(new StandardThreadFactory("tracingthreadfactory"));
    }

    /**
     * Creates a TracingThreadFactory with the given target ThreadFactory.
     *
     * @param targetFactory the ThreadFactory that is decorated.
     * @throws NullPointerException if targetFactory is null.
     */
    public TracingThreadFactory(ThreadFactory targetFactory) {
        if (targetFactory == null) throw new NullPointerException();
        this.targetFactory = targetFactory;
    }

    /**
     * Returns the ThreadFactory this TracingThreadFactory decorates.
     *
     * @return the ThreadFactory this TracingThreadFactory decorates.
     */
    public ThreadFactory getTargetFactory() {
        return targetFactory;
    }

    /**
     * Returns the number of created threads.
     *
     * @return the number of created threads.
     */
    public int getThreadCount() {
        return threadList.size();
    }

    /**
     * Returns a List containing all Threads that were created by this ThreadFactory.
     *
     * @return a List containing all Threads that were created by the ThreadFactory.
     */
    public List<Thread> getThreadList() {
        return threadList;
    }

    /**
     * Returns the number of alive threads.
     *
     * @return number of alive threads.
     */
    public int getAliveCount() {
        int count = 0;
        for (Thread thread : threadList) {
            if (thread.isAlive())
                count++;
        }
        return count;
    }

    /**
     * Returns the number of not alive Threads. A thread that is not started, also is not alive.
     *
     * @return number of terminated threads.
     */
    public int getNotAliveCount() {
        return getThreadCount() - getAliveCount();
    }

    public Thread newThread(Runnable r) {
        Thread t = targetFactory.newThread(r);
        threadList.add(t);
        return t;
    }

    /**
     * Asserts that the expected number of threads were created.
     *
     * @param expected the expected number of threads.
     * @throws IllegalArgumentException if expected is smaller than zero.
     */
    public void assertCreatedCount(int expected) {
        if (expected < 0) throw new IllegalArgumentException();
        assertEquals(expected, threadList.size());
    }

    /**
     * Asserts that no threads were created.
     */
    public void assertNoneCreated() {
        assertCreatedCount(0);
    }

    /**
     * Asserts that the given number of threads are created, and all of them are
     * not alive.
     *
     * @param expected the expected number of created and not alive threads.
     */
    public void assertCreatedAndNotAliveCount(int expected) {
        assertCreatedCount(expected);
        assertNotAliveCount(expected);
    }

    /**
     * Asserts that the given number of threads are created, and all of them are
     * alive.
     *
     * @param expected the expected number of created and alive threads.
     */
    public void assertCreatedAndAliveCount(int expected) {
        assertCreatedCount(expected);
        assertAliveCount(expected);
    }

    /**
     * Asserts that all threads created by the target ThreadFactory are not alive.
     *
     */
    public void assertAllAreNotAlive() {
        giveOthersAChance();
        for (Thread thread : threadList)
            assertNotAlive(thread);
    }


    /**
     * Asserts that all created threads are alive. If no threads are created,
     * this call doesn't fail.
     */
    public void assertAllAreAlive() {
        for (Thread thread : threadList)
            assertAlive(thread);
    }


    /**
     * Asserts that the given number of threads are alive. It doesn't say
     * anything about the number of terminated threads.
     *
     * @param expected the expected number of alive threads.
     */
    public void assertAliveCount(int expected) {
        assertEquals(expected, getAliveCount());
    }

    /**
     * Asserts that the given number of threads are not alive. It doesn't say
     * anything about the number of alive threads.
     *
     * @param expected the expected number of terminated threads.
     */
    public void assertNotAliveCount(int expected) {
        assertEquals(expected, getNotAliveCount());
    }
}
