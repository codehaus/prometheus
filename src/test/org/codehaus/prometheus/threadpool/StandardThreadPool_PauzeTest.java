package org.codehaus.prometheus.threadpool;

/**
 * Unittests the {@link org.codehaus.prometheus.threadpool.StandardThreadPool#pauze()} method.
 *
 * @author Peter Veentjer.
 */
public class StandardThreadPool_PauzeTest extends StandardThreadPool_AbstractTest{

    public void testWhileUnstarted(){
        fail();
    }

    public void testWhileIdle(){
        fail();
    }

    public void testWhileNotIdle(){
        fail();
    }

    public void testWhileShuttingDown(){
        fail();
    }

    public void testWhileShutdown(){
        fail();
    }
}
