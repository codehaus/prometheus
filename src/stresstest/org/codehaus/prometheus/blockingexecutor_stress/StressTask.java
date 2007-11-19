package org.codehaus.prometheus.blockingexecutor_stress;

import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.sleepRandom;
import org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil;

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
        ConcurrentTestUtil.someCalculation(100000);
    }
}