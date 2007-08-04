/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.testsupport;

import static org.codehaus.prometheus.testsupport.TestUtil.randomInt;

/**
 * A Runnable that stresses the CPU by doing some calculations.
 *
 * @author Peter Veentjer.
 */
public class StressRunnable implements Runnable{
    private final int iterations;

    public StressRunnable(){
        this(randomInt(1000));
    }

    public StressRunnable(int iterations) {
        this.iterations = iterations;
    }

    public void run() {
        TestUtil.someCalculation(iterations);
    }
}
