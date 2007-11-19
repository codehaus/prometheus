/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.giveOthersAChance;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.randomBoolean;
import org.codehaus.prometheus.concurrenttesting.Delays;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unittests {@link EndWorkerPolicy}.
 *
 * @author Peter Veentjer.
 */
public class EndWorkerPolicyTest extends ThreadPoolRepeater_AbstractTest {

    public void testRepeatableReturnsTrue() {
        int poolsize = 3;
        newRunningRepeater(poolsize);
        repeater.setRepeatableExecutionStrategy(new EndWorkerPolicy());

        DummyRepeatable task = new DummyRepeatable(true);
        spawned_repeat(task);

        giveOthersAChance(Delays.MEDIUM_MS);
        assertIsRunning();
        assertActualPoolSize(poolsize);
        assertDesiredPoolSize(poolsize);
        assertHasRepeatable(task);
    }

    public void testRepeatableReturnsFalse() {
        int poolsize = 6;
        int falsecount = 2;
        newRunningRepeater(poolsize);
        repeater.setRepeatableExecutionStrategy(new EndWorkerPolicy());

        RandomFalseRepeatable task = new RandomFalseRepeatable(falsecount);
        spawned_repeat(task);

        giveOthersAChance(Delays.MEDIUM_MS);
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
