package org.codehaus.prometheus.repeater;

/**
 * A task (just like a {@link Runnable}) that can be executed by a {@link Repeater}. The main
 * difference between a Runnable is that the {@link #execute()} is able to return a boolean value:
 * <ol>
 *  <li><tt>true</tt> indicates that if should be executed again</li>
 *  <li><tt>false</tt> indicates that it should not be runWork again</li>
 * </ol>
 * <p/>
 * 
 * It could be that a Repeatable is executed after it has returned <tt>false</tt>. So make sure that
 * the {@link #execute()} is able to deal with this situation.
 * <p/>
 *
 * todo:
 * remark about {@link org.codehaus.prometheus.repeater.RepeatableRunnable}.
 *
 * @author Peter Veentjer
 * @see  org.codehaus.prometheus.repeater.Repeater
 */
public interface Repeatable {

    /**
     * Executes this Repeatable.
     *
     * @return <tt>true</tt> if the task should be executed again, <tt>false</tt> otherwise.
     */
    boolean execute()throws Exception;
}
