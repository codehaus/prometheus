package org.codehaus.prometheus.processors.standardprocessor;

import org.codehaus.prometheus.processors.TestProcess;

/**
 * Unittests the {@link StandardProcessor}.
 */
public class StandardProcessor_OnceTest extends StandardProcessor_AbstractTest {

    public void testProcessHasNoMatchingReceive() {
        Integer arg = 10;

        TestProcess process = new TestProcess() {
            public void receive(String s) {
                fail();
            }
        };
        newProcessor(process);

        spawnedPut(arg);
        spawnedOnce(true);
        spawnedTake(arg);
        process.assertNotCalled();
    }

    public void testArgProcessReturnsVoid() {
        final Integer arg = 10;

        TestProcess process = new TestProcess() {
            public void receive(Integer i) {
                assertSame(arg, i);
                called = true;
            }
        };
        newProcessor(process);

        spawnedPut(arg);
        spawnedOnce(true);
        spawnedTake(arg);
        process.assertCalled();
    }

    public void testNoArgProcessReturnsValue() {
        final Integer value = 10;

        TestProcess process = new TestProcess() {
            public Integer receive() {
                called = true;
                return value;
            }
        };
        newSourceProcessor(process);

        spawnedOnce(true);
        spawnedTake(value);
        process.assertCalled();
    }

    public void testReceiveReturnsNull() {
        final Integer arg = 10;

        TestProcess process = new TestProcess() {
            public Object receive(Integer i) {
                assertSame(arg, i);
                called = true;
                return null;
            }
        };
        newProcessor(process);

        spawnedPut(arg);
        spawnedOnce(true);

        //todo: zou een take nu niet moeten mislukken?
        spawnedTake(arg);
        process.assertCalled();
    }

    public void testInputReturnsIterator() {
        //todo
    }

    public void testProcessReturnsIterator() {
        //todo
    }

    public void test_noInput_noOutput_noProcess() {
        //todo
    }

    public void test_onlyProcess() {
        //todo
    }

    public void test_multipleProcesses() {
        //todo
    }
}
