/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

/**
 * The DirectExecutorTest unittests the {@link DirectExecutor}.
 *
 * @author Peter Veentjer.
 */
public class DirectExecutorTest extends TestCase {

    private DirectExecutor directExecutor;
    private Runnable taskMock;

    public void setUp() {
        taskMock = createMock(Runnable.class);
        directExecutor = new DirectExecutor();
    }

    public void replayMocks() {
        replay(taskMock);
    }

    public void verifyMocks() {
        verify(taskMock);
    }

    public void testTaskIsNull() {
        try {
            directExecutor.execute(null);
            fail("NullPointerException expected");
        } catch (NullPointerException ex) {
            assertTrue(true);
        }
    }

    public void testTaskThrowsRuntimeException() {
        taskMock.run();
        expectLastCall().andThrow(new RuntimeException());

        replayMocks();
        try {
            directExecutor.execute(taskMock);
            fail("RuntimeException expected");
        } catch (RuntimeException ex) {
            assertTrue(true);
        }
        verifyMocks();
    }

    public void testSuccess() {
        taskMock.run();

        replayMocks();
        directExecutor.execute(taskMock);
        verifyMocks();
    }
}
