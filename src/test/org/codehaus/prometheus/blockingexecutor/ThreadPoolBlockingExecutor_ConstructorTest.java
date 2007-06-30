/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.exceptionhandler.NullExceptionHandler;
import org.codehaus.prometheus.testsupport.TracingThreadFactory;
import org.codehaus.prometheus.threadpool.StandardThreadPool;
import org.codehaus.prometheus.util.StandardThreadFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

/**
 * Unittests the constructors of {@link ThreadPoolBlockingExecutor}.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor_ConstructorTest extends ThreadPoolBlockingExecutor_AbstractTest {

    //================ ThreadPoolBlockingExecutor(int) =========================

    public void test_int() {
        try {
            new ThreadPoolBlockingExecutor(-1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }

        test_int(0);
        test_int(10);
    }

    private void test_int(int poolsize) {
        executor = new ThreadPoolBlockingExecutor(poolsize);

        assertIsUnstarted();
        assertHasDefaultBlockingQueue();
        assertTrue(executor.getWorkQueue().isEmpty());
        assertActualPoolSize(0);
        assertDesiredPoolSize(poolsize);
        assertHasDefaultThreadPool();
        assertHasDefaultExceptionHandler();
    }

    //================ ThreadPoolBlockingExecutor(int) =========================


    public void test_int_ThreadFactory_BlockingQueue() {
        try {
            new ThreadPoolBlockingExecutor(1, null, new LinkedBlockingQueue<Runnable>());
            fail();
        } catch (NullPointerException ex) {
        }

        try {
            new ThreadPoolBlockingExecutor(1, new StandardThreadFactory(), null);
            fail();
        } catch (NullPointerException ex) {
        }

        try {
            new ThreadPoolBlockingExecutor(-1, new StandardThreadFactory(), new LinkedBlockingQueue<Runnable>());
            fail();
        } catch (IllegalArgumentException ex) {
        }

        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
        threadFactory = new TracingThreadFactory();
        int poolsize = 10;
        executor = new ThreadPoolBlockingExecutor(poolsize, threadFactory, workQueue);

        assertIsUnstarted();
        assertSame(workQueue, executor.getWorkQueue());
        //assertHasThreadFactory(factory);
        assertNotNull(executor.getThreadPool() instanceof StandardThreadPool);
        StandardThreadPool pool = (StandardThreadPool) executor.getThreadPool();
        assertSame(threadFactory, pool.getThreadFactory());
        assertActualPoolSize(0);
        assertDesiredPoolSize(poolsize);
        assertTrue(executor.getWorkQueue().isEmpty());
        threadFactory.assertNoThreadsCreated();
        assertHasDefaultExceptionHandler();
    }

    private void assertHasDefaultThreadPool() {
        assertTrue(executor.getThreadPool() instanceof StandardThreadPool);
        ThreadFactory factory = ((StandardThreadPool) executor.getThreadPool()).getThreadFactory();
        assertTrue(factory instanceof StandardThreadFactory);
        StandardThreadFactory stdFactory = (StandardThreadFactory) factory;
        assertFalse(stdFactory.isProducingDaemons());
        assertEquals(Thread.NORM_PRIORITY, stdFactory.getPriority());
    }

    private void assertHasDefaultBlockingQueue() {
        assertNotNull(executor.getWorkQueue());
        BlockingQueue<Runnable> workQueue = executor.getWorkQueue();
        assertTrue(workQueue instanceof LinkedBlockingQueue);
        LinkedBlockingQueue linkedWorkQueue = (LinkedBlockingQueue) workQueue;
        assertEquals(Integer.MAX_VALUE, linkedWorkQueue.remainingCapacity());
    }

    private void assertHasDefaultExceptionHandler() {
        assertTrue(executor.getExceptionHandler() instanceof NullExceptionHandler);
    }
}

