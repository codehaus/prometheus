package org.codehaus.prometheus.processors.standardprocessor;

/**
 * The StopPolicy stops the current message and stops the process.
 * todo:
 * do other processes need to receive some sort of message? At the
 * moment other processes don't get any feedback.
 *
 * @author Peter Veentjer.
 */
public class StopPolicy implements Policy {
    public Object handle(Exception ex, Object... in) {
        throw new NullPointerException();
    }
}