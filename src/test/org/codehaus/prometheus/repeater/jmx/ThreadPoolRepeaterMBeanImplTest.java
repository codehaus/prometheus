/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater.jmx;

import junit.framework.TestCase;
import org.codehaus.prometheus.repeater.RepeaterServiceState;
import org.codehaus.prometheus.repeater.ThreadPoolRepeater;
import static org.easymock.EasyMock.expect;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

/**
 * Unittests the {@link ThreadPoolRepeaterMBeanImpl}.
 *
 * @author Peter Veentjer
 */
public class ThreadPoolRepeaterMBeanImplTest extends TestCase {
    private ThreadPoolRepeater repeaterMock;
    private ThreadPoolRepeaterMBeanImpl mbean;

    public void setUp() {
        repeaterMock = EasyMock.createMock(ThreadPoolRepeater.class);
        mbean = new ThreadPoolRepeaterMBeanImpl(repeaterMock);
    }

    public void replayMocks() {
        replay(repeaterMock);
    }

    public void verifyMocks() {
        verify(repeaterMock);
    }

    public void testConstructor() {
        try {
            new ThreadPoolRepeaterMBeanImpl(null);
            fail();
        } catch (NullPointerException foundThrowable) {
        }

        ThreadPoolRepeater repeater = new ThreadPoolRepeater(1);
        ThreadPoolRepeaterMBeanImpl mbean = new ThreadPoolRepeaterMBeanImpl(repeater);
        assertSame(repeater, mbean.getRepeater());
    }

    public void testGetActualPoolSize() {
        int actualPoolsize = 10;
        expect(repeaterMock.getActualPoolSize()).andReturn(actualPoolsize);
        replayMocks();
        int foundActualPoolsize = mbean.getActualPoolSize();
        verifyMocks();
        assertEquals(foundActualPoolsize, actualPoolsize);
    }

    public void testGetDesiredPoolSize() {
        int actualPoolsize = 10;
        expect(repeaterMock.getDesiredPoolSize()).andReturn(actualPoolsize);
        replayMocks();
        int foundActualPoolsize = mbean.getDesiredPoolSize();
        verifyMocks();
        assertEquals(foundActualPoolsize, actualPoolsize);
    }

    public void testSetDesiredPoolSize() {
        int actualPoolsize = 10;
        repeaterMock.setDesiredPoolSize(actualPoolsize);
        replayMocks();
        mbean.setDesiredPoolSize(actualPoolsize);
        verifyMocks();
    }

    public void testShutdown() {
        repeaterMock.shutdown();
        replayMocks();
        mbean.shutdown();
        verifyMocks();
    }

    public void testShutdownNow() {
        repeaterMock.shutdownNow();
        replayMocks();
        mbean.shutdownNow();
        verifyMocks();
    }

    public void testStart() {
        repeaterMock.start();
        replayMocks();
        mbean.start();
        verifyMocks();
    }

    public void testGetState() {
        RepeaterServiceState state = RepeaterServiceState.running;
        expect(repeaterMock.getState()).andReturn(state);
        replayMocks();
        RepeaterServiceState foundState = mbean.getState();
        verifyMocks();
        assertEquals(state, foundState);
    }
}
