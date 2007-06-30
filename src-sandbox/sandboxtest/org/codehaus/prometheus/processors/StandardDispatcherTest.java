package org.codehaus.prometheus.processors;

import junit.framework.TestCase;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Unittests the {@link StandardDispatcher}.
 *
 * @author Peter Veentjer.
 */
public class StandardDispatcherTest extends TestCase {
    private TestProcess process;
    private StandardDispatcher dispatcher;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dispatcher = new StandardDispatcher();
    }

    private void assertDispatchNoSuchMethod(Object... args) {
        try {
            dispatcher.dispatch(process, args);
            fail();
        } catch (NoSuchMethodException e) {
            process.assertNotCalled();
        } catch (Exception e) {
            fail();
        }
    }

    public void assertReceiveIsSuccess(Object expectedResult, Object arg) {
        try {
            Object result = dispatcher.dispatch(process, arg);
            assertEquals(expectedResult, result);
            process.assertCalledOnce();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void assertVoidReceiveIsSuccess(Object arg) {
        assertReceiveIsSuccess(VoidValue.INSTANCE, arg);
    }

    private void assertReceiveCausesInvocationTargetException(Exception ex) throws Exception {
        try {
            dispatcher.dispatch(process, VoidValue.INSTANCE);
            fail();
        } catch (InvocationTargetException ite) {
            assertSame(ex, ite.getCause());
        }
    }

    public void testArguments() throws Exception {
        try {
            dispatcher.dispatch(null, new DummyEvent());
            fail();
        } catch (NullPointerException ex) {
        }

        try {
            dispatcher.dispatch(new DummyProcess(), null);
            fail();
        } catch (NullPointerException ex) {
        }
    }

    public void testNoMatchingName() {
        process = new TestProcess() {
            public void foo(Integer e) {
                signalCalled();
            }
        };

        assertDispatchNoSuchMethod(10);
    }

    //this test needs to be defined better
    public void testAccessModifier_private() throws Exception {
        process = new TestProcess() {
            private void recieve(Integer e) {
                signalCalled();
            }
        };

        assertDispatchNoSuchMethod(10);
    }

    public void testAccessModifier_packageFriendly() {
        process = new TestProcess() {
            void receive(Integer e) {
                signalCalled();
            }
        };

        assertDispatchNoSuchMethod(10);
    }

    public void testAccessModifier_protected() {
        process = new TestProcess() {
            protected void receive(Integer e) {
                signalCalled();
            }
        };

        assertDispatchNoSuchMethod(10);
    }

    public void testNotEnoughArguments() throws Exception {
        process = new TestProcess() {
            public void receive() {
                signalCalled();
            }
        };

        assertDispatchNoSuchMethod(1);
    }

    public void testTooManyArguments() {
        process = new TestProcess() {
            public void receive(Integer arg1) {
                signalCalled();
            }
        };

        assertDispatchNoSuchMethod();
    }

    public void testArgumentIsNotOfCorrectType_completelyDifferentType() {
        process = new TestProcess() {
            public void handle(int foo) {
                signalCalled();
            }
        };

        assertDispatchNoSuchMethod("hello");
    }


    public void testArgumentIsNotOfCorrectType_subtype() {
        //todo
    }


    public void testThrowsRuntimeException() throws Exception {
        final RuntimeException ex = new RuntimeException() {
        };

        process = new TestProcess() {
            public void receive() {
                throw ex;
            }
        };

        assertReceiveCausesInvocationTargetException(ex);
    }

    public void testThrowsCheckedException() throws Exception {
        final Exception ex = new Exception() {
        };

        process = new TestProcess() {
            public void receive() throws Exception {
                throw ex;
            }
        };

        assertReceiveCausesInvocationTargetException(ex);
    }

    public void testSuperclassAlsoSearched() {
        //todo
    }

    public void testMostNarrowMatchIsMade() throws Exception {
        final List sendArg = new ArrayList();

        process = new TestProcess() {
            public void receive(List arg) {
                fail();
            }

            public void receive(ArrayList arg) {
                signalCalled();
                assertSame(sendArg, arg);
            }
        };

        assertVoidReceiveIsSuccess(sendArg);
    }

    public void testExactMatch_oneArg() throws Exception {
        final Integer sendArg = new Integer(10);

        process = new TestProcess() {
            public void receive(Integer e) {
                signalCalled();
                assertSame(sendArg, e);
            }
        };

        assertVoidReceiveIsSuccess(sendArg);
    }

    public void testExactMatch_noArg() throws Exception {
        process = new TestProcess() {
            public void receive() {
                signalCalled();
            }
        };

        assertVoidReceiveIsSuccess(VoidValue.INSTANCE);
    }

    public void testReturnValue() throws Exception {
        Integer sendArg = 10;
        Integer expectedResult = 20;

        process = new IntegerProcess(sendArg, expectedResult);

        assertReceiveIsSuccess(expectedResult, sendArg);
    }

    public void testSuperclassMatch() throws Exception {
        final LinkedList arg = new LinkedList();

        process = new TestProcess() {
            public void receive(Object e) {
                signalCalled();
                assertSame(arg, e);
            }

            public void receive(ArrayList e) {
                fail();
            }
        };

        assertVoidReceiveIsSuccess(arg);
    }

    public void testMatchOnInterface() {
        final LinkedList arg = new LinkedList();

        process = new TestProcess() {
            public void receive(List e) {
                signalCalled();
                assertSame(arg, e);
            }

            public void receive(ArrayList e) {
                fail();
            }
        };

        assertVoidReceiveIsSuccess(arg);
    }
}
