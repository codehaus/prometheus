package org.codehaus.prometheus.channels;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 *
 * @author Peter Veentjer.
 */
public class BufferedChannel<E> implements InputChannel<E>,OutputChannel<E>{
    private final BlockingQueue<E> queue;

    public BufferedChannel(){
        this(new LinkedBlockingQueue<E>());
    }

    public BufferedChannel(int capacity){
        this(new LinkedBlockingQueue<E>(capacity));
    }

    public BufferedChannel(BlockingQueue<E> queue){
        if(queue == null)throw new NullPointerException();
        this.queue = queue;
    }

    public E take() throws InterruptedException {
        return queue.take();
    }

    public E poll(long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
        E item = queue.poll(timeout,unit);
        if(item == null)
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
        queue.offer(item,timeout,unit);
        throw new RuntimeException();
    }
}
