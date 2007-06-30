package org.codehaus.prometheus.processors.standardprocessor; /**
 * The replace policy replaces the incoming message by a (static) instance.
 * <p/>
 * If you need to send back an dynamic instance instead of a static one, you need to
 * create your own instanceof of the {@link ErrorPolicy}.
 *
 * @author Peter Veentjer.
 */
public class Replace_ErrorPolicy implements ErrorPolicy {
    private final Object msg;

    public Replace_ErrorPolicy(Object msg) {
        this.msg = msg;
    }

    public Object handleReceiveError(Exception ex, Object in) {
        return msg;
    }
}
