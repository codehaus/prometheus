/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import org.codehaus.prometheus.concurrenttesting.TestRunnable;

import static java.util.Arrays.asList;
import java.util.List;

public class RunnableChain_ConstructorTest extends RunnableChain_AbstractTest {

    public void test_RunnableArrayIsNull() {
        try {
            new RunnableChain((Runnable[]) null);
            fail();
        } catch (NullPointerException ex) {
        }
    }

    public void test_RunnableArray() {
        Runnable r1 = new TestRunnable();
        Runnable r2 = new TestRunnable();
        Runnable r3 = new TestRunnable();

        runnableChain = new RunnableChain(r1, r2, r3);
        assertIsBreakable();
        assertChain(r1, r2, r3);
        assertHasDefaultExceptionHandler();
    }

    public void test_RunnableListIsNull() {
        try {
            new RunnableChain((List<Runnable>) null);
            fail();
        } catch (NullPointerException ex) {
        }
    }

    public void test_RunnableList() {
        Runnable r1 = new TestRunnable();
        Runnable r2 = new TestRunnable();
        Runnable r3 = new TestRunnable();

        runnableChain = new RunnableChain(asList(r1, r2, r3));
        assertIsBreakable();
        assertChain(r1, r2, r3);
        assertHasDefaultExceptionHandler();
    }
}
