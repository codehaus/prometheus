package org.codehaus.prometheus.processors.standardprocessor;

import org.codehaus.prometheus.processors.IntegerExceptionProcess;
import org.codehaus.prometheus.processors.IntegerProcess;
import org.codehaus.prometheus.processors.TestProcess;
import org.codehaus.prometheus.processors.ThrowingIterator;
import static org.codehaus.prometheus.testsupport.TestUtil.randomInt;

import java.awt.*;
import java.util.Iterator;

/**
 * Unittests all exception related functionality of the {@link StandardProcessor}.
 *
 * @author Peter Veentjer.
 */
public class StandardProcessor_ExceptionHandlingTest extends StandardProcessor_AbstractTest {
    private volatile RuntimeException uncheckedexception;
    private volatile Exception checkedexception;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        uncheckedexception = new RuntimeException() {
        };
        checkedexception = new Exception() {
        };
    }

    public void testSetter() {
        newProcessor(new TestProcess());
        ErrorPolicy policy = new Drop_ErrorPolicy();
        standardProcessor.setErrorPolicy(policy);
        assertSame(policy, standardProcessor.getErrorPolicy());
    }

    public void testSetter_nullArgument() {
        newProcessor(new TestProcess());

        try {
            standardProcessor.setErrorPolicy(null);
            fail();
        } catch (NullPointerException ex) {
        }
    }

    public void testErrorIsNotCaught() {
        Integer arg = 10;
        Error error = new AWTError("foo");
        TestProcess process = new IntegerExceptionProcess(arg, error);
        newProcessor(process);

        standardProcessor.setErrorPolicy(new Ignore_ErrorPolicy());

        spawned_assertPut(arg);
        spawned_assertOnceThrowsException(error);
        process.assertCalledOnce();
        spawned_assertTakeNotPossible();
    }

    //===================== Propagate_ErrorPolicy ====================

    public void testPropagate() {
        testPropagate(uncheckedexception);
        testPropagate(checkedexception);
    }

    private void testPropagate(final Exception ex) {
        Integer arg = 10;
        TestProcess process = new IntegerExceptionProcess(arg, ex);
        newProcessor(process);
        standardProcessor.setErrorPolicy(new Propagate_ErrorPolicy());

        spawned_assertPut(arg);
        spawned_assertOnceThrowsException(ex);
        process.assertCalledOnce();
    }

    //===================== Drop_ErrorPolicy ====================

    public void testDrop() {
        testDrop(uncheckedexception);
        testDrop(checkedexception);
    }

    public void testDrop(final Exception ex) {
        Integer arg = 10;
        TestProcess process = new IntegerExceptionProcess(arg, ex);
        newProcessor(process);
        standardProcessor.setErrorPolicy(new Drop_ErrorPolicy());

        spawned_assertPut(arg);
        spawned_assertOnceAndReturnTrue();
        process.assertCalledOnce();
        spawned_assertTakeNotPossible();
    }

    //===================== Ignore_ErrorPolicy ====================

    public void testIgnore() {
        testIgnore(uncheckedexception);
        testIgnore(checkedexception);
    }

    public void testIgnore(final Exception ex) {
        Integer arg = 10;
        TestProcess process = new IntegerExceptionProcess(arg, ex);
        newProcessor(process);
        standardProcessor.setErrorPolicy(new Ignore_ErrorPolicy());

        spawned_assertPut(arg);
        spawned_assertOnceAndReturnTrue();
        process.assertCalledOnce();
        spawned_assertTake(arg);
    }

    //===================== Replace_ErrorPolicy ====================

    public void testReplace() {
        testReplace(uncheckedexception);
        testIgnore(checkedexception);
    }

    public void testReplace(final Exception ex) {
        Integer arg = 10;
        TestProcess process = new IntegerExceptionProcess(arg, ex);
        newProcessor(process);
        Integer replaced = randomInt();
        standardProcessor.setErrorPolicy(new Replace_ErrorPolicy(replaced));

        spawned_assertPut(arg);
        spawned_assertOnceAndReturnTrue();

        //this can be simplified
        process.assertCalledOnce();
        spawned_assertTake(replaced);
    }

    //===================== OtherProblemAreas ====================

    public void testProcessCausesException() {
        //todo
    }

    public void testTakeFromInputCausesException() {
        //todo
    }

    public void testPutOnOutputCausesException() {
        //todo
    }

    public void testIteratorHasNextThrowsException(){
        
    }

    public void testIteratorNextThrowsException() {
        Integer initialValue = 1;
        Integer value1 = 1;
        Object value2 = new RuntimeException();
        Integer value3 = 10;
        Iterator it = new ThrowingIterator(value1, value2, value3);
        Integer value2Replacement = 5;

        TestProcess process = new IntegerProcess(initialValue, it);
        newProcessor(process);
        standardProcessor.setErrorPolicy(new Replace_ErrorPolicy(value2Replacement));

        spawned_assertPut(initialValue);
        spawned_assertOnceAndReturnTrue(3);
        process.assertCalledOnce();

        //this can be simplified.
        spawned_assertTake(value1);
        spawned_assertTake(value2Replacement);
        spawned_assertTake(value3);
        spawned_assertTakeNotPossible();
    }
}
