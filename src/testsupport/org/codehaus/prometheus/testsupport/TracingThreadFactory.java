package org.codehaus.prometheus.testsupport;

import static junit.framework.Assert.*;
import org.codehaus.prometheus.util.StandardThreadFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * A {@link ThreadFactory} that decorates a target ThreadFactory and registers all threads
 * that were created. It is useful when you want to keep track of the Threads that are
 * created by the target ThreadFactory.
 *
 * @author Peter Veentjer.
 */
public class TracingThreadFactory implements ThreadFactory {
    private final ThreadFactory targetFactory;
    private final List<Thread> threadList = Collections.synchronizedList(new LinkedList<Thread>());

    /**
     * Creates a TracingThreadFactory with a {@link StandardThreadFactory}.
     */
    public TracingThreadFactory(){
        this(new StandardThreadFactory());
    }

    /**
     * Creates a TracingThreadFactory with the given target ThreadFactory.
     *
     * @param targetFactory the ThreadFactory that is decorated.
     * @throws NullPointerException if targetFactory is null.
     */
    public TracingThreadFactory(ThreadFactory targetFactory){
        if(targetFactory == null)throw new NullPointerException();
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
    public int getThreadCount(){
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
     * Asserts that the expected number of threads were created.
     *
     * @param expected the expected number of threads.
     * @throws IllegalArgumentException if expected is smaller than zero.
     */
    public void assertCreatedCount(int expected){
        if(expected<0)throw new IllegalArgumentException();
        assertEquals(expected,threadList.size());
    }

    /**
     * Asserts that all threads created by the target ThreadFactory have terminated.
     */
    public void assertThreadsHaveTerminated(){
        //todo: also picks up unstarted ones
        for(Thread thread:threadList){
            assertFalse(String.format("Thread '%s' is still alive",thread),thread.isAlive());
        }
    }

    public Thread newThread(Runnable r) {
        Thread t = targetFactory.newThread(r);
        threadList.add(t);
        return t;
    }

    public void assertNoThreadsCreated() {
        assertCreatedCount(0);
    }

    public void assertAllThreadsAlive() {
        for(Thread thread:threadList)
            assertTrue(thread.isAlive());
    }
}
