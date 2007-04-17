package org.codehaus.prometheus.channels;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.BlockingQueue;

/**
 * Created by IntelliJ IDEA.
 * User: alarmnummer
 * Date: Apr 1, 2007
 * Time: 7:43:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class BufferedChannel<E> implements InputChannel<E>,OutputChannel<E>{
    private final BlockingQueue<E> queue;

    public BufferedChannel(BlockingQueue<E> queue){
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
