package org.codehaus.prometheus.repeater;

/**
 * Unittests {@link ThreadPoolRepeater#setRepeatableExecutionStrategy(RepeatableExecutionStrategy)}
 *
 * @author Peter Veentjer
 */
public class ThreadPoolRepeater_SetRepeatableExecutionStrategyTest extends ThreadPoolRepeater_AbstractTest{

    public void testSetNull(){
        newRunningStrictRepeater();

        RepeatableExecutionStrategy oldStrategy = repeater.getRepeatableExecutionStrategy();

        try{
            repeater.setRepeatableExecutionStrategy(null);
            fail();
        }catch(NullPointerException ex){
            assertHasExecuteRepeatableStrategy(oldStrategy);
        }
    }

    public void testSetNonNull(){
        newRunningStrictRepeater();

        RepeatableExecutionStrategy newStrategy = new EndRepeaterStrategy();
        repeater.setRepeatableExecutionStrategy(newStrategy);
        assertHasExecuteRepeatableStrategy(newStrategy);
    }
}
