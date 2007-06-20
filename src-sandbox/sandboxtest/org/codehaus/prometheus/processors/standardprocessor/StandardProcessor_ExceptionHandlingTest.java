package org.codehaus.prometheus.processors.standardprocessor;

import org.codehaus.prometheus.processors.TestProcess;

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

    public void testErrorIsNotCaught() {
        //todo
    }

    public void testPropagate() {
        testPropagate(uncheckedexception);
        testPropagate(checkedexception);
    }

    private void testPropagate(final Exception ex) {
        final Integer arg = 10;
        TestProcess process = new TestProcess() {
            public Object receive(Integer i) throws Exception {
                assertSame(arg, i);
                called = true;
                throw ex;
            }
        };
        newProcessor(process);
        standardProcessor.setPolicy(new PropagatePolicy());

        spawnedPut(arg);
        spawnedOnceThrowsException(ex);
        process.assertCalled();
    }

    public void testDrop() {
        testDrop(uncheckedexception);
        testDrop(checkedexception);
    }

    public void testDrop(final Exception ex) {
        final Integer arg = 10;
        TestProcess process = new TestProcess() {
            public Object receive(Integer i) throws Exception {
                assertSame(arg, i);
                called = true;
                throw ex;
            }
        };
        newProcessor(process);
        standardProcessor.setPolicy(new DropPolicy());

        spawnedPut(arg);
        spawnedOnce(true);
        process.assertCalled();
        //todo: no take possible
    }

    public void testIgnore() {
        testIgnore(uncheckedexception);
        testIgnore(checkedexception);
    }

    public void testIgnore(final Exception ex) {
        final Integer arg = 10;
        TestProcess process = new TestProcess() {
            public Object receive(Integer i) throws Exception {
                assertSame(arg, i);
                called = true;
                throw ex;
            }
        };
        newProcessor(process);
        standardProcessor.setPolicy(new IgnorePolicy());

        spawnedPut(arg);
        spawnedOnce(true);
        process.assertCalled();
        spawnedTake(arg);
    }

    public void testReplace() {
        testReplace(uncheckedexception);
        testIgnore(checkedexception);
    }

    public void testReplace(final Exception ex) {
        //todo
    }

    public void testTakeFromInputCausesException() {
        //todo
    }

    public void testPutOnOutputCausesException() {
        //todo
    }

    public void testIteratorThrowsException() {
        //todo
    }

}
