package org.codehaus.prometheus.channels;

/**
 * A {@link Channel} implementation with an internal capacity. In essence this is the same interface
 * as the {@link java.util.concurrent.BlockingQueue} provides.
 *
 * @author Peter Veentjer
 */
public interface BufferedChannel<E> extends Channel<E> {

    /**
     * Returns the current number of items in this BufferedChannel.  The returned
     * value could be stale as soon as it is received.
     *
     * @return the current number of items in this BufferedChannel.
     */
    int size();

    /**
     * Set the remaining capacity of this BufferedChannel.
     *
     * @param capacity the new remaining capacity                
     * @throw new UnsupportedOperation if the remaining capacity can't be changed.
     */
    void setRemainingCapacity(int capacity);

    /**
     * Returns the remaining capacity of this BufferedChannel.
     *
     * @return
     */
    int getRemainingCapacity();
}
