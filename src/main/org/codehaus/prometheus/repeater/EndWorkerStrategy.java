package org.codehaus.prometheus.repeater;

/**
 * A Policy that ends the current worker. It doesn't change the current tasks. It could lead
 * to a shutdown of the Repeater if the ThreadPool shutdown when it figures out that the
 * ThreadPool ends.
 *
 * @author Peter Veentjer.
 */
public class EndWorkerStrategy implements RepeatableExecutionStrategy {

    public final static EndWorkerStrategy INSTANCE = new EndWorkerStrategy();

    public boolean execute(Repeatable task, ThreadPoolRepeater repeater) throws Exception {
        try {
            return task.execute();
        } finally {
            repeater.lendableRef.takeback(task);
        }
    }
}
