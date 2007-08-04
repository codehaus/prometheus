/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import org.codehaus.prometheus.testsupport.DummyRunnable;

import static java.util.Arrays.asList;

public class RunnableChain_ConstructorTest extends RunnableChain_AbstractTest {

    public void test_RunnableArray() {
        Runnable r1 = new DummyRunnable();
        Runnable r2 = new DummyRunnable();
        Runnable r3 = new DummyRunnable();

        runnableChain = new RunnableChain(r1, r2, r3);
        assertIsBreakable();
        assertChain(r1, r2, r3);
        assertHasDefaultExceptionHandler();
    }

    public void test_RunnableList() {
        Runnable r1 = new DummyRunnable();
        Runnable r2 = new DummyRunnable();
        Runnable r3 = new DummyRunnable();

        runnableChain = new RunnableChain(asList(r1, r2, r3));
        assertIsBreakable();
        assertChain(r1, r2, r3);
        assertHasDefaultExceptionHandler();
    }
}
