/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

/**
 * An {@link ExecutionPolicy} that removes the current task that is repeated.
 * It doesn't change the state of worker.
 *
 * Todo: think about consequences
 *
 * @author Peter Veentjer
 * @since 0.1
 */
public class EndTaskPolicy implements ExecutionPolicy {

    public final static EndTaskPolicy INSTANCE = new EndTaskPolicy();

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
