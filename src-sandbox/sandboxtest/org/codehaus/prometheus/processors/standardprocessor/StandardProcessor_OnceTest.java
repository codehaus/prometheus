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

    public void test_noProcess() {
        Integer arg = 1;

        newProcessor(new Object[]{});

        spawnedPut(arg);
        spawnedOnce(true);
        spawnedTake(arg);
    }

    public void test_onlyProcess() {
        final Integer arg1 = 1;
        final Integer arg2 = 2;

        TestProcess process = new TestProcess() {
            public Object receive(Integer i) {
                assertSame(arg1, i);
                called = true;
                return arg2;
            }
        };


        newProcessor(new Object[]{process});

        spawnedPut(arg1);
        spawnedOnce(true);
        spawnedTake(arg2);
        process.assertCalled();
    }

    public void test_multipleProcesses() {
        final Integer arg1 = 1;
        final Integer arg2 = 2;
        final Integer arg3 = 3;
        final Integer arg4 = 4;

        TestProcess process1 = new TestProcess() {
            public Object receive(Integer i) {
                assertSame(arg1, i);
                called = true;
                return arg2;
            }
        };

        TestProcess process2 = new TestProcess() {
            public Object receive(Integer i) {
                assertSame(arg2, i);
                called = true;
                return arg3;
            }
        };

        TestProcess process3 = new TestProcess() {
            public Object receive(Integer i) {
                assertSame(arg3, i);
                called = true;
                return arg4;
            }
        };

        newProcessor(new Object[]{process1, process2, process3});

        spawnedPut(arg1);
        spawnedOnce(true);
        spawnedTake(arg4);
        process1.assertCalled();
        process2.assertCalled();
        process3.assertCalled();
    }
}
