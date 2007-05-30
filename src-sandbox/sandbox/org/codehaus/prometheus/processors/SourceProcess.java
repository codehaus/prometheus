package org.codehaus.prometheus.processors;

/**
 * A SourceProcess is a Process that generates output but recieves no output itself.
 *
 * problem with well process is that it should wait until
 *
 * @author Peter Veentjer.
 */
public interface SourceProcess<E> extends Process {

    E process()throws Exception;
}
