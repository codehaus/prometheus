package org.codehaus.prometheus.blockingexecutor;

/**
 * Unittests {@link ThreadPoolBlockingExecutor#setDesiredPoolSize(int)}
 *
 * @author Peter Veentjer
 */
public class ThreadPoolBlockingExecutor_SetDesiredPoolSizeTest extends ThreadPoolBlockingExecutor_AbstractTest {

    public void testIllegalArgument() {
        newStartedBlockingExecutor();
        int oldDesiredPoolSize = executor.getDesiredPoolSize();
        int oldActualPoolSize = executor.getActualPoolSize();

        spawned_assertSetDesiredPoolSizeThrowsException(-1,IllegalArgumentException.class);

        assertActualPoolSize(oldActualPoolSize);
        assertDesiredPoolSize(oldDesiredPoolSize);
        assertIsRunning();
    }

    public void testWhileUnstarted() {
        newUnstartedBlockingExecutor(100,1);

        int oldDesiredPoolSize = executor.getDesiredPoolSize();
        int oldActualPoolSize = executor.getActualPoolSize();

        int newPoolsize = oldDesiredPoolSize+10;
        spawned_assertSetDesiredPoolSize(newPoolsize);

        assertActualPoolSize(oldActualPoolSize);
        assertDesiredPoolSize(newPoolsize);
        assertIsUnstarted();
    }

    public void testWhileRunning_emptyPool() {
        testWhileRunning(0);
    }

    public void testWhileRunning_nonEmptyPool() {
        testWhileRunning(2);
    }
    
    public void testWhileRunning(int poolsize){
        newStartedBlockingExecutor(100,poolsize);

        int newPoolsize = poolsize+3;
        spawned_assertSetDesiredPoolSize(newPoolsize);

        assertActualPoolSize(newPoolsize);
        assertDesiredPoolSize(newPoolsize);
        assertIsRunning();
    }

    public void testWhileShuttingDown() {
        newShuttingDownBlockingExecutor(DELAY_EON_MS);

        int oldDesiredPoolSize = executor.getDesiredPoolSize();
        int oldActualPoolSize = executor.getActualPoolSize();

        spawned_assertSetDesiredPoolSizeThrowsException(10,IllegalStateException.class);

        assertActualPoolSize(oldActualPoolSize);
        assertDesiredPoolSize(oldDesiredPoolSize);
        assertIsShuttingDown();
    }

    public void testWhileShutdown() {
        newShutdownBlockingExecutor(100,1);
        int oldDesiredPoolSize = executor.getDesiredPoolSize();
        int oldActualPoolSize = executor.getActualPoolSize();

        spawned_assertSetDesiredPoolSizeThrowsException(10,IllegalStateException.class);

        assertActualPoolSize(oldActualPoolSize);
        assertDesiredPoolSize(oldDesiredPoolSize);
        assertIsShutdown();
    }
}
