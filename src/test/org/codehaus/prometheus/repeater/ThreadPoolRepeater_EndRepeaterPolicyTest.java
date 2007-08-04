/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

import static org.codehaus.prometheus.testsupport.TestUtil.giveOthersAChance;

/**
 * Unittests {@link EndRepeaterPolicy}.
 *
 * @author Peter Veentjer
 */
public class ThreadPoolRepeater_EndRepeaterPolicyTest extends ThreadPoolRepeater_AbstractTest {

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
        repeater.setRepeatableExecutionStrategy(new EndRepeaterPolicy());
    }
}
