/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor;

import java.util.List;

/**
 * Unittests the {@link ThreadPoolBlockingExecutor#shutdownNow()} method. 
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutor_ShutdownNowTest extends ThreadPoolBlockingExecutor_AbstractTest {

    public void testUnstartedWithTask(){
        fail();
    }

    public void testUnstartedWithoutTasks(){
        fail();
    }

    public void testRunningWithTask(){
        fail();
    }

    public void testRunningWithoutTask(){
        fail();
    }

    public void testUnstarted(){
        fail();
    }

    public void testShuttingDown(){
        newShuttingDownBlockingExecutor(1000);

        List<Runnable> tasks = executor.shutdownNow();
        assertIsShutdown();
        assertTrue(tasks.isEmpty());
    }

    public void testShutdown(){
        newShutdownBlockingExecutor(1,1);
        List<Runnable> tasks = executor.shutdownNow();
        assertIsShutdown();
        assertTrue(tasks.isEmpty());
    }
}
