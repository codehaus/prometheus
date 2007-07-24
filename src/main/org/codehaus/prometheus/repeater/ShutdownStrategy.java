package org.codehaus.prometheus.repeater;

/**
 * A Policy that shutsdown the ThreadPoolRepeater when a task returns false.
 *
 * @author Peter Veentjer
 */
public class ShutdownStrategy implements RepeatableExecutionStrategy {

    public final static ShutdownStrategy INSTANCE = new ShutdownStrategy();

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
