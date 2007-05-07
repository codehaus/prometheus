/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

/**
 * Unittests the constructors of {@link ThreadPoolBlockingExecutor}.
 *
 * @author Peter Veentjer.
 */
public  class ThreadPoolBlockingExecutor_ConstructorTest extends ThreadPoolBlockingExecutor_AbstractTest{


    public void testDummy(){}

    /*
    public void test_int() {
        try {
            new ThreadPoolBlockingExecutor(-1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }

        int poolsize = 120;
        ThreadPoolBlockingExecutor executor = new ThreadPoolBlockingExecutor(120);
        assertHasDefaultBlockingQueue(executor);
        assertTrue(executor.getWorkQueue().isEmpty());
        assertActualPoolSize(0);
        assertDesiredPoolSize(poolsize);
        assertHasDefaultThreadFactory(executor);
        assertEquals(BlockingExecutorServiceState.Unstarted, executor.getState());
    }

     private void assertHasDefaultThreadFactory(ThreadPoolBlockingExecutor executor) {
        ThreadFactory factory = executor.getThreadPool().getThreadFactory();
        assertNotNull(factory);
        assertTrue(factory instanceof StandardThreadFactory);
        StandardThreadFactory stdFactory = (StandardThreadFactory)factory;
        assertFalse(stdFactory.isProducingDaemons());
        assertEquals(Thread.NORM_PRIORITY,stdFactory.getPriority());        
    }

    private void assertHasDefaultBlockingQueue(ThreadPoolBlockingExecutor executor) {
        assertNotNull(executor.getWorkQueue());
        BlockingQueue<Runnable> workQueue = executor.getWorkQueue();
        assertTrue(workQueue instanceof LinkedBlockingQueue);
        LinkedBlockingQueue linkedWorkQueue = (LinkedBlockingQueue) workQueue;
        assertEquals(Integer.MAX_VALUE, linkedWorkQueue.remainingCapacity());
    }


    public void test_ThreadFactory_BlockingQueue_int() {
        try {
            new ThreadPoolBlockingExecutor(null, new LinkedBlockingQueue<Runnable>(), 1);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }

        try {
            new ThreadPoolBlockingExecutor(new StandardThreadFactory(), null, 1);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }

        try {
            new ThreadPoolBlockingExecutor(new StandardThreadFactory(), new LinkedBlockingQueue(), -1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }

        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
        ThreadFactory factory = new StandardThreadFactory();
        int poolsize = 10;
        ThreadPoolBlockingExecutor executor = new ThreadPoolBlockingExecutor(factory, workQueue, poolsize);
        assertSame(workQueue, executor.getWorkQueue());
        assertSame(poolsize, executor.getPoolSize());
        assertTrue(executor.getWorkQueue().isEmpty());
        assertSame(factory, executor.getThreadFactory());
        assertEquals(BlockingExecutorServiceState.Unstarted, executor.getState());
    } */
}
