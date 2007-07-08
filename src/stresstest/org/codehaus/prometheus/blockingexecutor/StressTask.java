package org.codehaus.prometheus.blockingexecutor;

import static org.codehaus.prometheus.testsupport.TestUtil.sleepRandom;
import static org.codehaus.prometheus.testsupport.TestUtil.someCalculation;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Some dummy task waits a certain amount of time (could be seen as io) and also does some
 * calculations.
 *
 * @author Peter Veentjer.
 */
public class StressTask implements Runnable {

    private final AtomicLong count;

    public StressTask(AtomicLong count){
        this.count = count;
    }

    public void run() {
        count.incrementAndGet();
        sleepRandom(20, TimeUnit.MILLISECONDS);
        //todo: random calculation
        someCalculation(100000);
    }
}