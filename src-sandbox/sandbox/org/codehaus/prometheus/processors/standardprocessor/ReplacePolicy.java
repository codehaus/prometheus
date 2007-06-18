package org.codehaus.prometheus.processors.standardprocessor;

/**
 * The replace policy sends the given message to the output. This
 * can be used to
 *
 * @author Peter Veentjer.
 */
public class ReplacePolicy implements ErrorPolicy {
    private final Object msg;

    public ReplacePolicy(Object msg) {
        this.msg = msg;
    }

    public Object handle(Exception ex, Object... in){
        return msg;
    }
}
