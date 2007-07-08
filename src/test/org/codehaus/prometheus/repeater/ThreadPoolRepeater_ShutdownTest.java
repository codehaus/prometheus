package org.codehaus.prometheus.repeater;

/**
 * Unittests {@link ThreadPoolRepeater#shutdown()}.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_ShutdownTest extends ThreadPoolRepeater_AbstractTest{

    public void testWhileUnstarted(){
        newShutdownRepeater();

        spawned_assertShutdown();
        assertIsShutdown();
    }

    public void testWhileRunning_emptyPool(){
        newRunningRepeater(0);

        spawned_assertShutdown();
        assertIsShutdown();
    }

    public void testWhileRunning_threadsAreBlocking(){
        //todo
    }

    public void testWhileRunning_threadsAreRunning(){
        //todo
    }

    public void testWhileShuttingDown_threadsAreRunning(){
        //todo
    }

    public void testWhileShuttingDown_threadsAreBlocking(){
        //todo
    }

    public void testWhileForcedShuttingdown(){
        //todo
    }

    public void testWhileShutdown(){
        newShutdownRepeater();

        spawned_assertShutdown();
        assertIsShutdown();
    }
}
