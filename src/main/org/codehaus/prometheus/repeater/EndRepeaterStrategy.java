/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

/**
 * A Policy that shutsdown the ThreadPoolRepeater when a task returns false.
 *
 * @author Peter Veentjer
 * @since 0.1
 */
public class EndRepeaterStrategy implements RepeatableExecutionStrategy {

    public final static EndRepeaterStrategy INSTANCE = new EndRepeaterStrategy();

    public boolean execute(Repeatable task, ThreadPoolRepeater repeater) throws Exception {
        try {
            boolean shutdown = !task.execute();
            if (shutdown)
                repeater.shutdown();

            return true;
        } finally {
            repeater.lendableRef.takeback(task);
        }
    }
}
