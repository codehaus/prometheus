package org.codehaus.prometheus.channels;

/**
 *
 *
 */
public interface BufferedChannel<E> extends Channel<E> {

    int size();

    void setRemainingCapacity(int capacity);

    int getRemainingCapacity();

}
