/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor.jmx;

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;
import org.codehaus.prometheus.blockingexecutor.BlockingExecutorService;
import org.codehaus.prometheus.blockingexecutor.BlockingExecutor;

/**
 * The BlockingExecutorServiceMBeanImplTest unittests the
 * {@link BlockingExecutorServiceMBeanImpl}.
 *
 * @author Peter Veentjer.
 */
public class BlockingExecutorServiceMBeanImplTest extends TestCase {
    private BlockingExecutor executorMock;

    public void testDummy(){}
    /*
    private BlockingExecutorServiceMBeanImpl mbean;

    public void setUp(){
        executorMock = createMock(BlockingExecutorService.class);
        mbean = new BlockingExecutorServiceMBeanImpl(executorMock);
    }

    public void replayMocks(){
        replay(executorMock);
    }

    public void verifyMocks(){
        verify(executorMock);
    }

    public void testConstructor(){
        try{
            new BlockingExecutorServiceMBeanImpl(null);
            fail("NullPointerException expected");
        }catch(NullPointerException ex){
            assertTrue(true);
        }
    }

    public void testStop(){
        executorMock.shutdown();
        replayMocks();
        mbean.stop();
        verifyMocks();
    }

    public void testStart(){
        executorMock.start();
        replayMocks();
        mbean.start();
        verifyMocks();
    } */
}
