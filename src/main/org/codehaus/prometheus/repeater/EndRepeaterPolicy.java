/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

/**
 * An {@link ExecutionPolicy} that shuts down the {@link ThreadPoolRepeater} when a task
 * returns <tt>false</tt>. It is useful when a ThreadPoolRepeater doesn't need to
 * run forever, and needs to shutdown when the task returns false.
 *
 * @author Peter Veentjer
 * @since 0.1
 */
public class EndRepeaterPolicy implements ExecutionPolicy {

    public final static EndRepeaterPolicy INSTANCE = new EndRepeaterPolicy();

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
