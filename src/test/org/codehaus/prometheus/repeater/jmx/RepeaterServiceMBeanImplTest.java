/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater.jmx;

import junit.framework.TestCase;
import static org.easymock.EasyMock.createMock;
import org.codehaus.prometheus.repeater.RepeaterService;

/**
 * Unittests the {@link ThreadPoolRepeaterMBeanImpl}.
 *
 * @author Peter Veentjer
 */
public class RepeaterServiceMBeanImplTest extends TestCase {
    private RepeaterService repeaterMock;
    private ThreadPoolRepeaterMBeanImpl mbean;

    public void setUp(){
        repeaterMock = createMock(RepeaterService.class);
  //      mbean = new ThreadPoolRepeaterMBeanImpl(repeaterMock);
    }

  public void testDummy(){}
/*
    public void replayMocks(){
        replay(repeaterMock);
    }

    public void verifyMocks(){
        verify(repeaterMock);
    }

    public void testConstructor() {
        try{
            new ThreadPoolRepeaterMBeanImpl(null);
            fail("NullPointerException expected");
        }catch(NullPointerException foundThrowable){
            assertTrue(true);
        }
    }

    public void testStop() {
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
        RepeaterServiceState expectedState = RepeaterServiceState.Running;
        expect(repeaterMock.getState()).andReturn(expectedState);

        replayMocks();
        RepeaterServiceState foundState = mbean.getState();
        verifyMocks();
        assertEquals(expectedState,foundState);
    }

    public void testGetPoolSize() {
        int expectedSize = 10;
        expect(repeaterMock.getPoolSize()).andReturn(expectedSize);

        replayMocks();
        int foundSize = mbean.getPoolSize();
        verifyMocks();
        assertEquals(expectedSize,foundSize);
    } */
}
