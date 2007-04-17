package org.codehaus.prometheus.threadpool;

import org.codehaus.prometheus.util.StandardThreadFactory;

import java.util.concurrent.ThreadFactory;

public class StandardThreadPool_ConstructorTest extends StandardThreadPool_AbstractTest {

    public void test_ThreadFactory(){
        try{
            new StandardThreadPool(null);
            fail();
        }catch(NullPointerException ex){
        }

        ThreadFactory factory = new StandardThreadFactory();
        threadpool = new StandardThreadPool(factory);
        assertIsUnstarted();
        assertSame(factory,threadpool.getThreadFactory());
        assertActualPoolsize(0);
        assertDesiredPoolsize(0);
        assertNull(threadpool.getDefaultWorkerJob());
    }
}
