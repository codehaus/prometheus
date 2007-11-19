/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

/**
 * <p>
 * A task, just like a {@link Runnable}, that can be executed by a {@link Repeater}. The main
 * difference between a Runnable is that the {@link #execute()} is allowed to throw an Exception and is
 * able to return a boolean value:
 * </p>
 * <ol>
 * <li><tt>true</tt> indicates that it should be executed again</li>
 * <li><tt>false</tt> indicates that it should not be executed again</li>
 * </ol>
 * <p>
 * It could be that a Repeatable is executed again after it has returned <tt>false</tt> the first
 * time. So make sure that the {@link #execute()} is able to deal with this situation.
 * <p/>
 *
 * @author Peter Veentjer
 * @see Repeater
 * @see ThreadPoolRepeater
 * @see RepeatableRunnable
 * @see EndRepeaterPolicy
 * @since 0.1
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
