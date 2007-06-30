package org.codehaus.prometheus.processors.standardprocessor;

import org.codehaus.prometheus.processors.IntegerProcess;
import org.codehaus.prometheus.processors.NoArgProcess;
import org.codehaus.prometheus.processors.TestProcess;
import org.codehaus.prometheus.processors.VoidValue;

import java.util.List;

/**
 * Unittests the {@link StandardProcessor}.
 *
 * @author Peter Veentjer.
 */
public class StandardProcessor_OnceTest extends StandardProcessor_AbstractTest {

    public void testNoMatchingReceive() {
        String arg = "foo";

        TestProcess process = new IntegerProcess();
        newProcessor(process);

        spawned_assertPut(arg);
        spawned_assertOnceAndReturnTrue();
        spawned_assertTake(arg);
        process.assertNotCalled();
        spawned_assertTakeNotPossible();
    }

    public void testArgProcessReturnsVoid() {
        Integer arg = 10;

        IntegerProcess process = new IntegerProcess(arg, VoidValue.INSTANCE);
        testArgProcessReturnsVoid(process, arg);
    }

    public void testArgProcessReturnsVoid(TestProcess process, Integer arg) {
        newProcessor(process);

        spawned_assertPut(arg);
        spawned_assertOnceAndReturnTrue();
        spawned_assertTake(arg);
        process.assertCalledOnce();
        spawned_assertTakeNotPossible();
    }

    public void testInputReturnsVoid() {
        TestProcess process = new NoArgProcess();
        newProcessor(process);

        spawned_assertPut(VoidValue.INSTANCE);
        spawned_assertOnceAndReturnTrue();
        spawned_assertTakeNotPossible();
        process.assertCalledOnce();
        spawned_assertTakeNotPossible();
    }

    public void testNoArgProcessReturnsValue() {
        Integer returned = 10;

        TestProcess process = new NoArgProcess(returned);
        newSourceProcessor(process);

        spawned_assertOnceAndReturnTrue();
        spawned_assertTake(returned);
        process.assertCalledOnce();
        spawned_assertTakeNotPossible();
    }

    public void testReceiveReturnsNull() {
        Integer arg = 10;

        TestProcess process = new IntegerProcess(arg, null);
        newProcessor(process);

        spawned_assertPut(arg);
        spawned_assertOnceAndReturnTrue();
        spawned_assertTakeNotPossible();
        process.assertCalledOnce();
    }

    public void testProcessReturnsIterator() {
        Integer arg = 10;
        List<Integer> itemList = generateRandomNumberList(20);
       
        TestProcess process = new IntegerProcess(arg, itemList.iterator());
        newProcessor(process);

        spawned_assertPut(arg);
        for (Integer item : itemList) {
            spawned_assertOnceAndReturnTrue();
            spawned_assertTake(item);
            spawned_assertTakeNotPossible();
        }
    }

    public void testInputReturnsIterator() {
        List<Integer> itemList = generateRandomNumberList(20);

        newProcessor(new Object[]{});

        spawned_assertPut(itemList.iterator());
        for (Integer item : itemList) {
            spawned_assertOnceAndReturnTrue();
            spawned_assertTake(item);
            spawned_assertTakeNotPossible();
        }
    }

    public void testChainedProcessesThatReturnIterators() {
        /*
        final Integer arg = 10;

        final List<Integer> item1List = generateRandomNumberList();
        final List<Integer> item2List = generateRandomNumberList();

        TestProcess process1 = new TestProcess() {
            public Iterator<Integer> receive(Integer i) {
                assertSame(arg, i);
                signalCalled();
                return item1List.iterator();
            }
        };

        TestProcess process2 = new TestProcess() {
            public Iterator<Integer> receive(Integer i) {
                assertSame(arg, i);
                signalCalled();
                return item2List.iterator();
            }
        };

        newProcessor(new Object[]{process1, process2});

        spawned_assertPut(arg);
        spawned_assertOnce(true);
        //check that all items have been outputted.
        for (Integer item : itemList1){
            spawned_assertTake(item);
        }
          */
    }


    public void test_noInput_noOutput_noProcess() {
        newProcessor(-1, -1, new Object[]{});
        spawned_assertOnceAndReturnTrue();
    }

    public void test_noProcess() {
        Integer arg = 1;

        newProcessor(new Object[]{});

        spawned_assertPut(arg);
        spawned_assertOnceAndReturnTrue();
        spawned_assertTake(arg);
        spawned_assertTakeNotPossible();
    }

    public void test_onlyProcess() {
        Integer arg1 = 1;
        Integer arg2 = 2;

        TestProcess process = new IntegerProcess(arg1, arg2);
        newProcessor(new Object[]{process});

        spawned_assertPut(arg1);
        spawned_assertOnceAndReturnTrue();
        spawned_assertTake(arg2);
        process.assertCalledOnce();
        spawned_assertTakeNotPossible();
    }

    public void test_multipleProcesses() {
        Integer arg1 = 1;
        Integer arg2 = 2;
        Integer arg3 = 3;
        Integer arg4 = 4;

        TestProcess process1 = new IntegerProcess(arg1, arg2);
        TestProcess process2 = new IntegerProcess(arg2, arg3);
        TestProcess process3 = new IntegerProcess(arg3, arg4);

        newProcessor(new Object[]{process1, process2, process3});

        spawned_assertPut(arg1);
        spawned_assertOnceAndReturnTrue();
        spawned_assertTake(arg4);
        process1.assertCalledOnce();
        process2.assertCalledOnce();
        process3.assertCalledOnce();
        spawned_assertTakeNotPossible();
    }
}
