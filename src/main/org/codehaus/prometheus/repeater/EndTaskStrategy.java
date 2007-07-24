package org.codehaus.prometheus.repeater;

/**
 * An {@link RepeatableExecutionStrategy} that removes the current task that is repeated.
 * It doesn't change the state of worker.
 *
 * If the ThreadPoolRepeater is relaxed, it could lead to the
 *
 * @author Peter Veentjer
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
