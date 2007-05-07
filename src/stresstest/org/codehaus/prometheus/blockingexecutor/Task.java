package org.codehaus.prometheus.blockingexecutor;

import org.codehaus.prometheus.testsupport.TestUtil;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Some dummy task waits a certain amount of time (could be seen as io) and also does some
 * calculations.
 *
 * @author Peter Veentjer.
 */
public class Task implements Runnable {
    private final AtomicInteger count;

    public Task(){
        this(new AtomicInteger());
    }

    public Task(AtomicInteger count){
        this.count = count;
    }

    public void run() {
        System.out.println("task executed count: "+count);
        count.incrementAndGet();
        System.out.println("1");
        TestUtil.sleepRandom(20, TimeUnit.MILLISECONDS);
        System.out.println("2");
        TestUtil.someCalculation(100000);
        System.out.println("3");
    }
}