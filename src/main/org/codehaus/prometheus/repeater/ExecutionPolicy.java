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
 * @author Peter Veentjer
 * @since 0.1
 */
public interface ExecutionPolicy {

    /**
     * @param task
     * @param repeater
     * @return true if the executing worker should stay alive, and false if it should
     *         terminate itself.
     * @throws Exception
     */
    boolean execute(Repeatable task, ThreadPoolRepeater repeater) throws Exception;
}
