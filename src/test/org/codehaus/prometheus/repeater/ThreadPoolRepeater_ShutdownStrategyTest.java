package org.codehaus.prometheus.repeater;

import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;

/**
 * Unittests {@link ShutdownStrategy}.
 *
 * @author Peter Veentjer
 */
public class ThreadPoolRepeater_ShutdownStrategyTest extends ThreadPoolRepeater_AbstractTest {

    public void testRepeatableReturnsTrue() {
        int poolsize = 3;
        newRepeater(poolsize);

        DummyRepeatable task = new DummyRepeatable(true);
        spawned_repeat(task);

        giveOthersAChance(DELAY_MEDIUM_MS);

        assertActualPoolSize(poolsize);
        assertDesiredPoolSize(poolsize);
        assertIsRunning();
        assertHasRepeatable(task);
    }

    public void testRepeatableReturnsFalse() {
        int poolsize = 3;
        newRepeater(poolsize);

        DummyRepeatable task = new DummyRepeatable(false);
        spawned_repeat(task);

        giveOthersAChance(DELAY_MEDIUM_MS);

        assertActualPoolSize(0);
        assertDesiredPoolSize(poolsize);
        assertIsShutdown();
        assertHasRepeatable(task);
    }

    private void newRepeater(int poolsize) {
        newRunningRepeater(poolsize);
        repeater.setRepeatableExecutionStrategy(new ShutdownStrategy());
    }
}
