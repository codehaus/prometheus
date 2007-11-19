/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

/**
 * A Policy that is used to determine how to deal with the execution of
 * a Repeatable task. A repeatable task can return true/false or throw an
 * checked or unchecked exception.
 *
 * Implementations of the ExecutionPolicy are tied to the {@link ThreadPoolRepeater}.
 * This is something that needs to be improved.
 *
 * todo: make inner class of inside ThreadPoolRepeater. This interface already is tied to it.
 *
 * @author Peter Veentjer
 * @since 0.1
 */
public interface ExecutionPolicy {

    /**
     *
     *
     * @param task the Repeatable that is executed
     * @param repeater  the ThreadPoolRepeater that is executing the task
     * @return true if the executing worker should stay alive, and false if it should
     *         terminate itself.
     * @throws Exception thrown by the task. Implementations are allowed to
     */
    boolean execute(Repeatable task, ThreadPoolRepeater repeater) throws Exception;
}
