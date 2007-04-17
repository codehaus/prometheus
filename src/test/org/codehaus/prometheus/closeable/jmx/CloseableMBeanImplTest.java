/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.closeable.jmx;

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;
import org.codehaus.prometheus.closeable.Closeable;
import org.codehaus.prometheus.waitpoint.CloseableWaitpoint;

/**
 * Because the CloseableMBeanImpl is just a delegate from a
 * {@link org.codehaus.prometheus.closeable.Closeable} instance,
 * just a simple mock test is good enough because the class
 * doesn't contain any logic. So simply touching the mockmethods
 * is a good enough test.
 *
 * @author Peter Veentjer.
 */
public class CloseableMBeanImplTest extends TestCase {
    private Closeable closeableMock;
    private CloseableMBeanImpl mbean;

    public void setUp() {
        closeableMock = createMock(Closeable.class);
        mbean = new CloseableMBeanImpl(closeableMock);
    }

    public void replayMocks() {
        replay(closeableMock);
    }

    public void verifyMocks() {
        verify(closeableMock);
    }

    public void testConstructor() {
        try{
            new CloseableMBeanImpl(null);
            fail();
        }catch(NullPointerException ex){
            assertTrue(true);
        }

        Closeable closeable = new CloseableWaitpoint();
        CloseableMBeanImpl mbean = new CloseableMBeanImpl(closeable);
        assertSame(closeable, mbean.getCloseable());
    }

    public void test_isOpen() {
        expect(closeableMock.isOpen()).andReturn(true);
        replayMocks();
        assertTrue(mbean.isOpen());
        verifyMocks();
    }

    public void test_isClosed() {
        expect(closeableMock.isClosed()).andReturn(true);
        replayMocks();
        assertTrue(mbean.isClosed());
        verifyMocks();
    }

    public void test_open() throws InterruptedException {
        closeableMock.open();

        replayMocks();
        mbean.open();
        verifyMocks();
    }


    public void testClose() throws InterruptedException {
        closeableMock.close();

        replayMocks();
        mbean.close();
        verifyMocks();
    }

}
