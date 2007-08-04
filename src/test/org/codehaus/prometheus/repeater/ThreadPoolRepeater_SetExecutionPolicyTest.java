/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.repeater;

/**
 * Unittests {@link ThreadPoolRepeater#setRepeatableExecutionStrategy(ExecutionPolicy)}
 *
 * @author Peter Veentjer
 */
public class ThreadPoolRepeater_SetExecutionPolicyTest extends ThreadPoolRepeater_AbstractTest{

    public void testSetNull(){
        newRunningStrictRepeater();

        ExecutionPolicy oldStrategy = repeater.getExecutionPolicy();

        try{
            repeater.setRepeatableExecutionStrategy(null);
            fail();
        }catch(NullPointerException ex){
            assertHasExecutionPolicy(oldStrategy);
        }
    }

    public void testSetNonNull(){
        newRunningStrictRepeater();

        ExecutionPolicy newStrategy = new EndRepeaterPolicy();
        repeater.setRepeatableExecutionStrategy(newStrategy);
        assertHasExecutionPolicy(newStrategy);
    }
}
