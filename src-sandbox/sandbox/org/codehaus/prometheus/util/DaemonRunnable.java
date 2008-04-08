package org.codehaus.prometheus.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * A {@link Runnable} tailored for daemon tasks: a task that needs te be executed over and over.
 * <p/>
 * <pre>
 * class FooDaemonRunnable extends DaemonRunnable{
 *      public void runOnce(){
 *          ..logic
 *      }
 * }
 * </pre>
 *
 * @author Peter Veentjer
 */
public abstract class DaemonRunnable implements Runnable {

    //todo: needs to be replaced by commons logging
    private final static Logger logger = LogManager.getLogger(DaemonRunnable.class);

    private volatile boolean stop;

    private volatile int delay;

    private volatile int maxFrequency;

    private volatile TimeUnit timeUnit = TimeUnit.SECONDS;

    /**
     * Stops the daemon. The current execution is not interrupted. Repeated calls are ignored.
     */
    public void stop() {
        stop = true;
    }

    /**
     * Stios the daemon and interrupts the execution of the current runOnce. Repeated calls are
     * ignored.
     */
    public void stopNow() {
        throw new RuntimeException("not yet implemented");
    }

    public boolean hasStopBeenCalled() {
        return stop;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        if (timeUnit == null) throw new NullPointerException();
        this.timeUnit = timeUnit;
    }

    public int getDelay() {
        return delay;
    }

    /**
     *
     * @param delay
     * @throws IllegalArgumentException if delay smaller than 0.
     */
    public void setDelay(int delay) {
        if (delay < 0) throw new IllegalArgumentException();
        this.delay = delay;
    }

    public int getMaxFrequency() {
        return maxFrequency;
    }

    /**
     *
     * @param maxFrequency
     * @throws IllegalArgumentException if maxFrequency smaller than zero.
     */
    public void setMaxFrequency(int maxFrequency) {
        if (maxFrequency < 0) throw new IllegalArgumentException();
        this.maxFrequency = maxFrequency;
    }

    public void run() {
        while (!stop) {
            try {
                runOnce();
            } catch (Exception ex) {
                logger.error("An error occurred while executed the runOnce", ex);
            }
        }
    }

    protected abstract void runOnce();
}
