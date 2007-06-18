package org.codehaus.prometheus.channels;

/**
 *
 * A Channel also can be seen as a pipe, see 'Pipes and Filters' pattern described in
 * 'Patterns of Software Architecture Volume 1' for more information.
 *
 * @author Peter Veentjer.
 */
public interface Channel<E> extends InputChannel<E>,OutputChannel<E>{
}
