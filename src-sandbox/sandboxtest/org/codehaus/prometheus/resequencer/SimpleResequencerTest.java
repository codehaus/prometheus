package org.codehaus.prometheus.resequencer;

import org.codehaus.prometheus.concurrenttesting.ConcurrentTestCase;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.sleepRandomMs;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleResequencerTest extends ConcurrentTestCase {

    public void testFoo() throws InterruptedException {
        BlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
        SimpleResequencer<Integer> resequencer = new SimpleResequencer<Integer>(queue, new ReentrantLock());
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();

        int threadcount = 3;
        int count = 5;
        ExecutorService executor = new ThreadPoolExecutor(threadcount, threadcount, 0, TimeUnit.NANOSECONDS, workQueue);

        for (int k = 0; k < count; k++) {
            Long id = resequencer.nextSequenceId();
            SomeTask runnable = new SomeTask(resequencer, id, k);
            executor.execute(runnable);
        }
        executor.shutdown();
        executor.awaitTermination(6000, TimeUnit.SECONDS);

        assertSequencedQueue(queue, count);
    }

    private void assertSequencedQueue(BlockingQueue<Integer> queue, int size) {
        assertEquals(size, queue.size());
        Integer[] array = queue.toArray(new Integer[size]);
        System.out.println("output ");
        for (int k = 0; k < size; k++) {
            assertEquals(new Integer(k), array[k]);
            System.out.println(array[k]);
        }
    }

    public class SomeTask implements Runnable {
        private final Long sequenceId;
        private final Integer load;
        private final Resequencer resequencer;

        public SomeTask(Resequencer resequencer, Long sequenceId, Integer load) {
            this.resequencer = resequencer;
            this.sequenceId = sequenceId;
            this.load = load;
        }

        public void run() {
            sleepRandomMs(5000);
            try {
                resequencer.put(sequenceId, load);
                System.out.println("load:" + load);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}
