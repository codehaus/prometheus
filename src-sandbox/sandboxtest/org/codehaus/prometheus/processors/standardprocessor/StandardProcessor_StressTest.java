package org.codehaus.prometheus.processors.standardprocessor;

import org.codehaus.prometheus.processors.TestProcess;
import org.codehaus.prometheus.testsupport.TestThread;
import org.codehaus.prometheus.testsupport.TestUtil;
import static org.codehaus.prometheus.testsupport.TestUtil.randomInt;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Stresstests the {@link StandardProcessor}.
 *
 * the once thread is not waiting the correct number of times. This has to do with
 * the thread could execute different frames (so frames created by other threads).
 * So it could do a wait for input that is never comming. So the test needs to be
 * changed.
 *
 * @author Peter Veentjer.
 */
public class StandardProcessor_StressTest extends StandardProcessor_AbstractTest {

    public void test() {
        List<Integer> itemList = generateRandomNumberList(10000);
        BlockingQueue<Integer> itemQueue = new LinkedBlockingQueue(itemList);

        System.out.println("finished generating data");

        TestProcess process = new StressProcess();
        newProcessor(process);
        
        long startNs = System.currentTimeMillis();

        List<TestThread> threadList = new LinkedList<TestThread>();
        while (!itemQueue.isEmpty()) {
            int count = randomInt(itemList.size()/4);
            if (count > itemList.size())
                count = itemList.size();

            Vector v = new Vector();
            itemQueue.drainTo(v, count);

            spawned_assertPut(v);

            TestThread testThread = scheduleOnce(v.size());
            System.out.println("starting thread: "+testThread+" number:"+(threadList.size()+1)+" count: "+v.size());
            threadList.add(testThread);
        }

        try {
            for (Thread thread : threadList)
                joinAll(6000 * 1000, thread);
        } finally {
            System.out.println("retrieved items"+outputChannel.getInternalQueue().size());
        }

        long endMs = System.currentTimeMillis();
        long elapsedMs = endMs - startNs;
        double elapsedSec = elapsedMs/1000.0d;
        double speed = itemList.size()*1.0 / elapsedSec;
        System.out.println("elapsed seconds: " + elapsedSec);
        System.out.println("items: " + itemList.size());
        System.out.println("speed: " + speed);
    }

    public OnceThread scheduleOnce(int count) {
        OnceThread onceThread = new OnceThread(count);
        onceThread.start();
        return onceThread;
    }

    class OnceThread extends TestThread {
        private final int count;

        public OnceThread(int count) {
            this.count = count;
        }

        protected void runInternal() throws Exception {
            for (int k = 0; k < count; k++) {
                TestUtil.someCalculation(10000);


                if(k%100==0)
                Thread.yield();
                if(k%1000==0)
                    System.out.println("thread "+this+" k="+k);
                boolean result = standardProcessor.once();
                assertTrue(result);
            }
            System.out.println("finished thread "+this+" count:"+count);
        }
    }

    public static class StressProcess extends TestProcess {

        public Iterator receive(Vector list) {
            return list.iterator();
        }
    }
}
