/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.concurrenttesting;

import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.randomInt;

/**
 * A Runnable that stresses the CPU by doing some calculations.
 *
 * @author Peter Veentjer.
 */
public class StressRunnable extends TestRunnable{
    private final int iterations;

    public StressRunnable(){
        this(randomInt(1000));
    }

    public StressRunnable(int iterations) {
        this.iterations = iterations;
    }

    @Override
    public void runInternal() {
        ConcurrentTestUtil.someCalculation(iterations);
    }
}
