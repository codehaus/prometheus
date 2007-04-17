package org.codehaus.prometheus.resequencer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * A {@link Resequencer} where puts block as long as the item to be placed is out of sequence.
 *
 * @author Peter Veentjer.
 */
public class SimpleResequencer<E> implements Resequencer<Long, E> {

    private final AtomicLong sequence = new AtomicLong(Long.MIN_VALUE);
    private final Lock mainLock;
    private final Condition newplacementCondition;
    private final BlockingQueue<E> blockingQueue;
    private long expectedId = sequence.get();

    public SimpleResequencer(BlockingQueue<E> blockingQueue, Lock mainLock) {
        if (blockingQueue == null || mainLock == null) throw new NullPointerException();
        this.blockingQueue = blockingQueue;
        this.mainLock = mainLock;
        this.newplacementCondition = mainLock.newCondition();
    }

    public void put(Long sequenceId, E item) throws InterruptedException {
        if (sequenceId == null) throw new NullPointerException();

        mainLock.lock();
        try {
            if (sequenceId < expectedId)
                throw new IllegalArgumentException();
            if (sequenceId > sequence.get())
                throw new IllegalArgumentException();
            while (sequenceId != expectedId)
                newplacementCondition.await();

            blockingQueue.put(item);

            //no more interruptible calls allowed, would leave the
            //system in an inconsistent state
            expectedId++;
            newplacementCondition.signalAll();
        } finally {
            mainLock.unlock();
        }
    }

    public Long nextSequenceId() {
        return sequence.getAndIncrement();
    }
}
