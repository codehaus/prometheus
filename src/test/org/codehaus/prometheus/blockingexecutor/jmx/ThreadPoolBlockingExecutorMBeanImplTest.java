/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.blockingexecutor.jmx;

import junit.framework.TestCase;
import org.codehaus.prometheus.blockingexecutor.BlockingExecutorServiceState;
import org.codehaus.prometheus.blockingexecutor.ThreadPoolBlockingExecutor;
import static org.easymock.classextension.EasyMock.*;

import java.util.LinkedList;

/**
 * Unittests the {@link ThreadPoolBlockingExecutorMBean}.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolBlockingExecutorMBeanImplTest extends TestCase {
    private ThreadPoolBlockingExecutor executorMock;
    private ThreadPoolBlockingExecutorMBeanImpl mbean;

    public void setUp() {
        executorMock = createMock(ThreadPoolBlockingExecutor.class);
        mbean = new ThreadPoolBlockingExecutorMBeanImpl(executorMock);
    }

    public void replayMocks() {
        replay(executorMock);
    }

    public void verifyMocks() {
        verify(executorMock);
    }

    public void testConstructor() {
        try {
            new ThreadPoolBlockingExecutorMBeanImpl(null);
            fail();
        } catch (NullPointerException ex) {
        }

        ThreadPoolBlockingExecutor executor = new ThreadPoolBlockingExecutor(1);
        ThreadPoolBlockingExecutorMBeanImpl mbean = new ThreadPoolBlockingExecutorMBeanImpl(executor);
        assertSame(executor, mbean.getExecutor());
    }

    public void testGetActualPoolSize() {
        int actualPoolsize = 10;
        expect(executorMock.getActualPoolSize()).andReturn(actualPoolsize);
        replayMocks();
        int foundActualPoolsize = mbean.getActualPoolSize();
        verifyMocks();
        assertEquals(foundActualPoolsize, actualPoolsize);
    }

    public void testGetDesiredPoolSize() {
        int actualPoolsize = 10;
        expect(executorMock.getDesiredPoolSize()).andReturn(actualPoolsize);
        replayMocks();
        int foundActualPoolsize = mbean.getDesiredPoolSize();
        verifyMocks();
        assertEquals(foundActualPoolsize, actualPoolsize);
    }

    public void testSetDesiredPoolSize() {
        int actualPoolsize = 10;
        executorMock.setDesiredPoolSize(actualPoolsize);
        replayMocks();
        mbean.setDesiredPoolSize(actualPoolsize);
        verifyMocks();
    }

    public void testShutdown() {
        expect(executorMock.shutdownPolitly()).andReturn(new LinkedList<Runnable>());

        replayMocks();
        mbean.shutdown();
        verifyMocks();
    }

    public void testShutdownNow() {
        expect(executorMock.shutdownNow()).andReturn(new LinkedList<Runnable>());
        replayMocks();
        mbean.shutdownNow();
        verifyMocks();
    }

    public void testStart() {
        executorMock.start();
        replayMocks();
        mbean.start();
        verifyMocks();
    }

    public void testGetState() {
        BlockingExecutorServiceState state = BlockingExecutorServiceState.Running;
        expect(executorMock.getState()).andReturn(state);
        replayMocks();
        BlockingExecutorServiceState foundState = mbean.getState();
        verifyMocks();
        assertEquals(state, foundState);
    }
}
