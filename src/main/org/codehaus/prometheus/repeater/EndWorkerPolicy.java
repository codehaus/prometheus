/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

/**
 * A {@link ExecutionPolicy} that ends the current worker-thread but doesn't influence
 * other workers. It is useful when a single worker needs to terminate when the task
 * returns false.
 *
 * @author Peter Veentjer.
 * @since 0.1
 */
public class EndWorkerPolicy implements ExecutionPolicy {

    public final static EndWorkerPolicy INSTANCE = new EndWorkerPolicy();

    public boolean execute(Repeatable task, ThreadPoolRepeater repeater) throws Exception {
        try {
            //false indicates that the worker thread should terminate itself.
            return task.execute();
        } finally {
            repeater.lendableRef.takeback(task);
        }
    }
}
