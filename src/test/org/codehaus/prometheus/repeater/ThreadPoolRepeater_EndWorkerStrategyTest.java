package org.codehaus.prometheus.repeater;

import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;
import static org.codehaus.prometheus.testsupport.TestUtil.randomBoolean;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unittests {@link EndWorkerStrategy}.
 *
 * @author Peter Veentjer.
 */
public class ThreadPoolRepeater_EndWorkerStrategyTest extends ThreadPoolRepeater_AbstractTest {

    public void testRepeatableReturnsTrue() {
        int poolsize = 3;
        newRunningRepeater(poolsize);
        repeater.setRepeatableExecutionStrategy(new EndWorkerStrategy());

        DummyRepeatable task = new DummyRepeatable(true);
        spawned_repeat(task);

        giveOthersAChance(DELAY_MEDIUM_MS);
        assertIsRunning();
        assertActualPoolSize(poolsize);
        assertDesiredPoolSize(poolsize);
        assertHasRepeatable(task);
    }

    public void testRepeatableReturnsFalse() {
        int poolsize = 6;
        int falsecount = 2;
        newRunningRepeater(poolsize);
        repeater.setRepeatableExecutionStrategy(new EndWorkerStrategy());

        RandomFalseRepeatable task = new RandomFalseRepeatable(falsecount);
        spawned_repeat(task);

        giveOthersAChance(DELAY_MEDIUM_MS);
        assertIsRunning();
        assertActualPoolSize(poolsize-falsecount);
        //desiredpoolsize
        assertHasRepeatable(task);
    }

    public class RandomFalseRepeatable implements Repeatable {
        private final AtomicInteger falseCount = new AtomicInteger();

        public RandomFalseRepeatable(int falsecount) {
            falseCount.set(falsecount);
        }

        public boolean execute() throws Exception {
            boolean returnfalse = randomBoolean();
            if (returnfalse) {
                int remainingfalsecount = falseCount.decrementAndGet();
                return remainingfalsecount < 0;
            } else {
                return true;
            }
        }
    }
}
