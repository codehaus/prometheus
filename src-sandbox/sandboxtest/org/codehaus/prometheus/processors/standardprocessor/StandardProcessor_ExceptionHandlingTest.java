package org.codehaus.prometheus.processors.standardprocessor;

import org.codehaus.prometheus.processors.IntegerExceptionProcess;
import org.codehaus.prometheus.processors.IntegerProcess;
import org.codehaus.prometheus.processors.TestProcess;
import static org.codehaus.prometheus.testsupport.TestUtil.randomInt;

import static java.util.Arrays.asList;
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

        try {
            standardProcessor.setErrorPolicy(null);
            fail();
        } catch (NullPointerException ex) {
        }
    }

    public void testErrorIsNotCaught() {
        //todo
    }

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

    public void testTakeFromInputCausesException() {
        //todo
    }

    public void testPutOnOutputCausesException() {
        //todo
    }

    public void testIteratorThrowsException() {
        Integer initialValue = 1;
        Integer value1 = 1;
        Object value2 = new RuntimeException();
        Integer value3 = 10;
        Iterator it = asList(value1, value2, value3).iterator();
        Integer value2Replacement = 5;

        TestProcess process = new IntegerProcess(initialValue, it);
        newProcessor(process);
        standardProcessor.setErrorPolicy(new Replace_ErrorPolicy(value2Replacement));

        spawned_assertPut(initialValue);
        spawned_assertOnceAndReturnTrue();
        process.assertCalledOnce();

        //this can be simplified.
        spawned_assertTake(value1);
        spawned_assertTake(value2Replacement);
        spawned_assertTake(value3);
        spawned_assertTakeNotPossible();
    }
}
