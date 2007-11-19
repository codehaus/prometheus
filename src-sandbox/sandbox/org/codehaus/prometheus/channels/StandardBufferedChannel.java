package org.codehaus.prometheus.channels;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A {@link Channel} that can buffer message (messages that were added, but not retrieved). The
 * BufferedChannel uses a {@link BlockingQueue} as internal storage mechnism.
 *
 * @author Peter Veentjer.
 */
public class StandardBufferedChannel<E> implements BufferedChannel<E> {
    private final LinkedBlockingQueue<E> queue;

    /**
     * Creates a new unbounded BufferedChannel.
     */
    public StandardBufferedChannel() {
        this(new LinkedBlockingQueue<E>());
    }

    /**
     * Creates a new bounded BufferedChannel.
     *
     * @param capacity the capacity of the buffer.
     * @throws IllegalArgumentException if <tt>capacity</tt> is not greater than zero
     */
    public StandardBufferedChannel(int capacity) {
        this(new LinkedBlockingQueue<E>(capacity));
    }

    /**
     * Creates a new BufferedChannel that uses the given queue as internal storage mechanism.
     *
     * @param queue
     * @throws NullPointerException if queue is null.
     */
    public StandardBufferedChannel(BlockingQueue<E> queue) {
        if (queue == null) throw new NullPointerException();
        //todo; nasty hack
        this.queue = (LinkedBlockingQueue) queue;
    }

    public int size() {
        return queue.size();
    }

    public void setRemainingCapacity(int capacity) {
        throw new UnsupportedOperationException();
    }

    public int getRemainingCapacity() {
        return queue.remainingCapacity();
    }


    /**
     * Returns the internal BlockingQueue this BufferedChannel to store unretrieved messages.
     *
     * @return the internal BlockingQueue.
     */
    public BlockingQueue<E> getInternalQueue() {
        return queue;
    }

    public E take() throws InterruptedException {
        return queue.take();
    }

    public E poll(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        E item = queue.poll(timeout, unit);
        if (item == null)
            throw new TimeoutException();
        return item;
    }

    public E poll() {
        return queue.poll();
    }

    public E peek() {
        return queue.peek();
    }

    public void put(E item) throws InterruptedException {
        queue.put(item);
    }

    public long offer(E item, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        long startNs = System.nanoTime();

        if (!queue.offer(item, timeout, unit))
            throw new TimeoutException();

        return startNs - System.nanoTime();
    }
}
