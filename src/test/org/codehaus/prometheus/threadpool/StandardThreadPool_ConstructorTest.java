package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.exceptionhandler.NullExceptionHandler;
import org.codehaus.prometheus.testsupport.TracingThreadFactory;
import org.codehaus.prometheus.util.StandardThreadFactory;

import java.util.concurrent.ThreadFactory;

/**
 * Unittests the constructors of {@link StandardThreadPool}.
 *
 * @author Peter Veentjer
 */
public class StandardThreadPool_ConstructorTest extends StandardThreadPool_AbstractTest {

    //================== StandardThreadPool(int) ========================

    public void test_noArg() {
        threadpool = new StandardThreadPool();
        assertIsUnstarted();
        assertActualPoolsize(0);
        assertDesiredPoolsize(0);
        assertStandardThreadFactory();
        assertHasNoDefaultWorkerJob();
        assertHasNullExceptionHandler();
    }

    //================== StandardThreadPool(int) ========================

    public void test_int() {
        try {
            new StandardThreadPool(-1);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        test_int(0);
        test_int(10);
    }

    private void test_int(int poolsize) {
        threadpool = new StandardThreadPool(poolsize);
        assertIsUnstarted();
        assertActualPoolsize(0);
        assertDesiredPoolsize(poolsize);
        assertStandardThreadFactory();
        assertHasNoDefaultWorkerJob();
        assertHasNullExceptionHandler();
    }

    //================== StandardThreadPool(int,ThreadFactory) ========================

    public void test_int_ThreadFactory() {
        try {
            new StandardThreadPool(-1, new StandardThreadFactory());
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            new StandardThreadPool(1, null);
            fail();
        } catch (NullPointerException e) {
        }

        test_int_ThreadFactory(0);
        test_int_ThreadFactory(10);
    }

    public void test_int_ThreadFactory(int poolsize) {
        TracingThreadFactory factory = new TracingThreadFactory();
        threadpool = new StandardThreadPool(poolsize, factory);
        assertIsUnstarted();
        assertSame(factory, threadpool.getThreadFactory());
        assertActualPoolsize(0);
        assertDesiredPoolsize(poolsize);
        assertHasNoDefaultWorkerJob();
        assertHasNullExceptionHandler();
        factory.assertNoThreadsCreated();
    }

    //================== StandardThreadPool(ThreadFactory) ========================

    public void test_ThreadFactory() {
        try {
            new StandardThreadPool(null);
            fail();
        } catch (NullPointerException ex) {
        }

        TracingThreadFactory factory = new TracingThreadFactory();
        threadpool = new StandardThreadPool(factory);
        assertIsUnstarted();
        assertSame(factory, threadpool.getThreadFactory());
        assertActualPoolsize(0);
        assertDesiredPoolsize(0);
        assertHasNoDefaultWorkerJob();
        assertHasNullExceptionHandler();
        factory.assertNoThreadsCreated();
    }

    //================== StandardThreadPool(WorkerJob,ThreadFactory) ========================

    public void test_WorkerJob_ThreadFactory() {
        try {
            new StandardThreadPool(new DummyWorkerJob(), null);
            fail();
        } catch (NullPointerException ex) {
        }

        test_WorkerJob_ThreadFactory(null);
        test_WorkerJob_ThreadFactory(new DummyWorkerJob());
    }

    public void test_WorkerJob_ThreadFactory(WorkerJob workerJob) {
        TracingThreadFactory factory = new TracingThreadFactory();
        threadpool = new StandardThreadPool(workerJob, factory);
        assertIsUnstarted();
        assertSame(factory, threadpool.getThreadFactory());
        assertActualPoolsize(0);
        assertDesiredPoolsize(0);
        assertSame(workerJob, threadpool.getDefaultWorkerJob());
        assertHasNullExceptionHandler();
        factory.assertNoThreadsCreated();
    }

    //================== StandardThreadPool(int,WorkerJob,ThreadFactory) ========================

    public void test_int_WorkerJob_ThreadFactory() {
        try {
            new StandardThreadPool(-1, new DummyWorkerJob(), null);
            fail();
        } catch (IllegalArgumentException ex) {
        }


        try {
            new StandardThreadPool(1, new DummyWorkerJob(), null);
            fail();
        } catch (NullPointerException ex) {
        }

        test_int_WorkerJob_ThreadFactory(0,null);
        test_int_WorkerJob_ThreadFactory(0,new DummyWorkerJob());
        test_int_WorkerJob_ThreadFactory(1,null);
        test_int_WorkerJob_ThreadFactory(1,new DummyWorkerJob());
    }

    public void test_int_WorkerJob_ThreadFactory(int poolsize, WorkerJob workerJob) {
        TracingThreadFactory factory = new TracingThreadFactory();
        threadpool = new StandardThreadPool(poolsize,workerJob, factory);
        assertIsUnstarted();
        assertSame(factory, threadpool.getThreadFactory());
        assertActualPoolsize(0);
        assertDesiredPoolsize(poolsize);
        assertSame(workerJob, threadpool.getDefaultWorkerJob());
        assertHasNullExceptionHandler();
        factory.assertNoThreadsCreated();
    }

    //======================== asserts =========================

    private void assertHasNullExceptionHandler() {
        assertTrue(threadpool.getExceptionHandler() instanceof NullExceptionHandler);
    }

    private void assertHasNoDefaultWorkerJob() {
        assertNull(threadpool.getDefaultWorkerJob());
    }

    private void assertStandardThreadFactory() {
        ThreadFactory factory = threadpool.getThreadFactory();
        assertTrue(factory instanceof StandardThreadFactory);
        StandardThreadFactory stdFactory = (StandardThreadFactory) factory;
        assertEquals(Thread.NORM_PRIORITY, stdFactory.getPriority());
        assertFalse(stdFactory.isProducingDaemons());
    }
}
