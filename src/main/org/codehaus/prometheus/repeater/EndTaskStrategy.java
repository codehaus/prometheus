/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

/**
 * An {@link RepeatableExecutionStrategy} that removes the current task that is repeated.
 * It doesn't change the state of worker.
 *
 * If the ThreadPoolRepeater is relaxed, it could lead to the
 *
 * @author Peter Veentjer
 * @since 0.1
 */
public class EndTaskStrategy implements RepeatableExecutionStrategy {

    public final static EndTaskStrategy INSTANCE = new EndTaskStrategy();

    public boolean execute(Repeatable task, ThreadPoolRepeater repeater) throws Exception {
        boolean again = true;
        try {
            again = task.execute();
            return true;
        } finally {
            if (again)
                repeater.lendableRef.takeback(task);
            else
                repeater.lendableRef.takebackAndReset(task);
        }
    }
}
