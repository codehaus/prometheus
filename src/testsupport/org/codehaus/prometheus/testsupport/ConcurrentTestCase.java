/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import junit.framework.TestCase;

/**
 * A TestCase that contains various concurrency related functions.
 * <p/>
 * Design note: Originally a lot of logic was part of this TestCase. But forcing
 * some base classed to use is not very convenient. A lot of base classes for
 * Test support already are available (springtestcases, rmocktestcases) and to prevent
 * these structures to be used, I decided to extract most logic to seperate packages so
 * they can be used in combination with existing base test-classes.
 *
 * @author Peter Veentjer.
 */
public abstract class ConcurrentTestCase extends TestCase {

    public static final boolean START_INTERRUPTED = true;
    public static final boolean START_UNINTERRUPTED = false;

    public volatile Stopwatch stopwatch;

    public ConcurrentTestCase() {
    }

    public ConcurrentTestCase(String fixture) {
        super(fixture);
    }

    @Override
    public void setUp() throws Exception {
        stopwatch = new Stopwatch();
    }
}

