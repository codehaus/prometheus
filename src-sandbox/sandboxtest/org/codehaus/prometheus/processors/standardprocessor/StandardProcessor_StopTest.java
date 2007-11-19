package org.codehaus.prometheus.processors.standardprocessor;

import org.codehaus.prometheus.processors.IntegerProcess;
import org.codehaus.prometheus.processors.TestProcess;

/**
 * Unittests the stop functionality of the {@link StandardProcessor}.
 *
 * @author Peter Veentjer.
 */
public class StandardProcessor_StopTest extends StandardProcessor_AbstractTest {

    //todo
    public void _testNoProcessSinkProcessor(){
        int arg = 10;
        newSinkProcessor(new Object[]{});

        standardProcessor.setStopPolicy(new IntegerStopPolicy(arg));

        spawned_assertPut(arg);
        spawned_assertOnceAndReturnFalse();
        spawned_assertTakeNotPossible();
    }

    //todo
    public void _testNoProcess() {
        int arg = 10;
        newPipedProcessor(new Object[]{});

        standardProcessor.setStopPolicy(new IntegerStopPolicy(arg));

        spawned_assertPut(arg);
        spawned_assertOnceAndReturnFalse();
        spawned_assertTake(arg);
        spawned_assertTakeNotPossible();
    }

    public void testInputReturnsStopableItem() {
        //todo
    }

    public void testReceiveExceptionIsTransformedToStopableMessage() {
       //todo
    }

    public void _testOnlyProcessReturnsStopableItem() {
        int arg1 = 10;
        int arg2 = 20;
        TestProcess process = new IntegerProcess(arg1, arg2);
        newPipedProcessor(process);

        standardProcessor.setStopPolicy(new IntegerStopPolicy(arg2));

        spawned_assertPut(arg1);
        spawned_assertOnceAndReturnFalse();
        spawned_assertTake(arg2);
        process.assertCalledOnce();
        spawned_assertTakeNotPossible();
    }

    public void testFirstProcessReturnsStopableItem() {

    }

    public void testIteratorAndStopableItems() {

    }

    //there are more cases that need to be tested
}
