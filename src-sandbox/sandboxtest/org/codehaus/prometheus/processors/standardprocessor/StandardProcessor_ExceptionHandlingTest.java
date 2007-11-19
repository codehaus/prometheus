package org.codehaus.prometheus.processors.standardprocessor;

import org.codehaus.prometheus.processors.IntegerExceptionProcess;
import org.codehaus.prometheus.processors.IntegerProcess;
import org.codehaus.prometheus.processors.TestProcess;
import org.codehaus.prometheus.processors.ThrowingIterator;
import static org.codehaus.prometheus.concurrenttesting.ConcurrentTestUtil.randomInt;

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
        newPipedProcessor(new TestProcess());
        ErrorPolicy policy = new Drop_ErrorPolicy();
        standardProcessor.setErrorPolicy(policy);
        assertSame(policy, standardProcessor.getErrorPolicy());
    }

    public void testSetter_nullArgument() {
        newPipedProcessor(new TestProcess());

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
        newPipedProcessor(process);

        standardProcessor.setErrorPolicy(new Ignore_ErrorPolicy());

        spawned_assertPut(arg);
        spawned_assertOnceThrowsException(error);
        process.assertCalledOnce();
        spawned_assertTakeNotPossible();
    }

    //===================== Propagate_ErrorPolicy ====================

    public void testPropagate_ErrorPolicy() {
        testPropagate_errorPolicy(uncheckedexception);
        testPropagate_errorPolicy(checkedexception);
    }

    private void testPropagate_errorPolicy(final Exception ex) {
        Integer arg = 10;
        TestProcess process = new IntegerExceptionProcess(arg, ex);
        newPipedProcessor(process);
        standardProcessor.setErrorPolicy(new Propagate_ErrorPolicy());

        spawned_assertPut(arg);
        spawned_assertOnceThrowsException(ex);
        process.assertCalledOnce();
    }

    //===================== Drop_ErrorPolicy ====================

    public void testDrop_ErrorPolicy() {
        testDrop_ErrorPolicy(uncheckedexception);
        testDrop_ErrorPolicy(checkedexception);
    }

    public void testDrop_ErrorPolicy(final Exception ex) {
        Integer arg = 10;
        TestProcess process = new IntegerExceptionProcess(arg, ex);
        newPipedProcessor(process);
        standardProcessor.setErrorPolicy(new Drop_ErrorPolicy());

        spawned_assertPut(arg);
        spawned_assertOnceAndReturnTrue();
        process.assertCalledOnce();
        spawned_assertTakeNotPossible();
    }

    //===================== Ignore_ErrorPolicy ====================

    public void testIgnore_ErrorPolicy() {
        testIgnore_ErrorPolicy(uncheckedexception);
        testIgnore_ErrorPolicy(checkedexception);
    }

    public void testIgnore_ErrorPolicy(final Exception ex) {
        Integer arg = 10;
        TestProcess process = new IntegerExceptionProcess(arg, ex);
        newPipedProcessor(process);
        standardProcessor.setErrorPolicy(new Ignore_ErrorPolicy());

        spawned_assertPut(arg);
        spawned_assertOnceAndReturnTrue();
        process.assertCalledOnce();
        spawned_assertTake(arg);
    }

    //===================== Replace_ErrorPolicy ====================

    public void testReplace_ErrorPolicy() {
        testReplace_ErrorPolicy(uncheckedexception);
        testIgnore_ErrorPolicy(checkedexception);
    }

    public void testReplace_ErrorPolicy(final Exception ex) {
        Integer arg = 10;
        TestProcess process = new IntegerExceptionProcess(arg, ex);
        newPipedProcessor(process);
        Integer replaced = randomInt();
        standardProcessor.setErrorPolicy(new Replace_ErrorPolicy(replaced));

        spawned_assertPut(arg);
        spawned_assertOnceAndReturnTrue();

        //this can be simplified
        process.assertCalledOnce();
        spawned_assertTake(replaced);
    }

    //===================== OtherProblemAreas ====================
    // make sure that messages are not thrown, but passed as message
    //============================================================

    public void testProcessCausesException() {
        Integer value1 = 1;
        Exception value2 = new RuntimeException();

        TestProcess process = new IntegerExceptionProcess(value1, value2);
        newPipedProcessor(process);
        standardProcessor.setErrorPolicy(new ExceptionAsMessage_ErrorPolicy());

        spawned_assertPut(value1);
        spawned_assertOnceAndReturnTrue(1);
        process.assertCalledOnce();
        spawned_assertTake(value2);
        spawned_assertTakeNotPossible();
    }

    public void testTakeFromInputCausesException() {
        //todo
    }

    public void testPutOnOutputCausesException() {
        //todo
    }

    public void testIteratorHasNextThrowsException() {
        //todo (at the moment already tested with getNext)
    }

    public void _testIteratorNextThrowsException() {
        Integer initialValue = 1;
        Integer value1 = 1;
        Object value2 = new RuntimeException();
        Integer value3 = 10;
        Iterator it = new ThrowingIterator(value1, value2, value3);

        TestProcess process = new IntegerProcess(initialValue, it);
        newPipedProcessor(process);
        standardProcessor.setErrorPolicy(new ExceptionAsMessage_ErrorPolicy());

        spawned_assertPut(initialValue);
        spawned_assertOnceAndReturnTrue(3);
        process.assertCalledOnce();

        //this can be simplified.
        spawned_assertTake(value1);
        spawned_assertTake(value2);
        spawned_assertTake(value3);
        spawned_assertTakeNotPossible();
    }
}
