package org.codehaus.prometheus.repeater;

/**
 * <p>
 * A task, just like a {@link Runnable}, that can be executed by a {@link Repeater}. The main
 * difference between a Runnable is that the {@link #execute()} is able to return a boolean value:
 * </p>
 * <ol>
 * <li><tt>true</tt> indicates that if should be executed again</li>
 * <li><tt>false</tt> indicates that it should not be executed again</li>
 * </ol>
 *
 * <p>
 * It also allows checked exceptions to be thrown (instead of just runtime exceptions).
 * </p>
 *
 * <p>
 * It could be that a Repeatable is executed again after it has returned <tt>false</tt> the first
 * time. So make sure that the {@link #execute()} is able to deal with this situation.
 * <p/>
 *
 * @author Peter Veentjer
 * @see org.codehaus.prometheus.repeater.Repeater
 * @see org.codehaus.prometheus.repeater.RepeatableRunnable
 */
public interface Repeatable {

    /**
     * Executes this Repeatable.
     *
     * @return <tt>true</tt> if the task should be executed again, <tt>false</tt> otherwise.
     * @throws Exception execute is allowed to throw exceptions (also unchecked).
     */
    boolean execute() throws Exception;
}
