package org.codehaus.prometheus.processors;

/**
 * A WellProcess is a Process that generates output but recieves no output itself.
 *
 * problem with well process is that it should wait until
 */
public interface WellProcess<E> extends Process {

    E process()throws Exception;
}
