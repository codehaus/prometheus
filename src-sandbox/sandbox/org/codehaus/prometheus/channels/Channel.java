package org.codehaus.prometheus.channels;

/**
 * A Channel also can be seen as a pipe, see 'Pipes and Filters' pattern described in
 * 'Patterns of Software Architecture Volume 1' for more information.
 * <p/>
 * The big difference between this Channel and the {@link java.nio.channels.Channel} is that
 * the former is object oriented and the latter is byte oriented; so more low level.
 *
 * @author Peter Veentjer.
 */
public interface Channel<E> extends InputChannel<E>, OutputChannel<E> {
}
