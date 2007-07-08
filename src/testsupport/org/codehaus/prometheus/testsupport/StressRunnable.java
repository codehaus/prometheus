package org.codehaus.prometheus.testsupport;

public class StressRunnable implements Runnable{
    private final int iterations;

    public StressRunnable(int iterations) {
        this.iterations = iterations;
    }

    public void run() {
        TestUtil.someCalculation(iterations);
    }
}
