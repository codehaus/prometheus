package org.codehaus.prometheus.processors;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.lang.reflect.InvocationTargetException;

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

    public void assertDispatchSuccess(Object expectedResult, Object... args) {
        try {
            Object result = dispatcher.dispatch(process, args);
            assertEquals(expectedResult, result);
            process.assertCalled();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    public void assertVoidDispatchSuccess(Object... args) {
        assertDispatchSuccess(Void.INSTANCE, args);
    }

    private void assertDispatchCausesInvocationTargetException(Exception ex) throws Exception {
        try {
            dispatcher.dispatch(process);
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
                called = true;
            }
        };

        assertDispatchNoSuchMethod(10);
    }


    //this test needs to be defined better
    public void testAccessModifier_private() throws Exception {
        process = new TestProcess() {
            private void recieve(Integer e) {
                called = true;
            }
        };

        assertDispatchNoSuchMethod(10);
    }

    public void testAccessModifier_packageFriendly() {
        process = new TestProcess() {
            void receive(Integer e) {
                called = true;
            }
        };

        assertDispatchNoSuchMethod(10);
    }

    public void testAccessModifier_protected() {
        process = new TestProcess() {
            protected void receive(Integer e) {
                called = true;
            }
        };

        assertDispatchNoSuchMethod(10);
    }

    public void testNotEnoughArguments() throws Exception {
        process = new TestProcess() {
            public void receive() {
                called = true;
            }
        };

        assertDispatchNoSuchMethod(1);
    }

    public void testTooManyArguments() {
        process = new TestProcess() {
            public void receive(Integer arg1) {
                called = true;
            }
        };

        assertDispatchNoSuchMethod();
    }

    public void testArgumentIsNotOfCorrectType_completelyDifferentType() {
        process = new TestProcess() {
            public void handle(int foo) {
                called = true;
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

        assertDispatchCausesInvocationTargetException(ex);
    }

    public void testThrowsCheckedException() throws Exception {
        final Exception ex = new Exception() {
        };

        process = new TestProcess() {
            public void receive() throws Exception {
                throw ex;
            }
        };

        assertDispatchCausesInvocationTargetException(ex);
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
                called = true;
                assertSame(sendArg, arg);
            }
        };

        assertVoidDispatchSuccess(sendArg);
    }

    public void testExactMatch_oneArg() throws Exception {
        final Integer sendArg = new Integer(10);

        process = new TestProcess() {
            public void receive(Integer e) {
                called = true;
                assertSame(sendArg, e);
            }
        };

        assertVoidDispatchSuccess(sendArg);
    }

    public void testExactMatch_multipleArguments() throws Exception {
        final Object sendArg1 = 1;
        final Object sendArg2 = 2;
        final Object sendArg3 = 3;

        process = new TestProcess() {
            public void receive(Integer arg1, Integer arg2, Integer arg3) {
                called = true;
                assertSame(sendArg1, arg1);
                assertSame(sendArg2, arg2);
                assertSame(sendArg3, arg3);
            }
        };

        assertVoidDispatchSuccess(sendArg1, sendArg2, sendArg3);
    }

    public void testExactMatch_noArg() throws Exception {
        process = new TestProcess() {
            public void receive() {
                called = true;
            }
        };

        assertVoidDispatchSuccess();
    }

    public void testReturnValue() throws Exception {
        final Integer sendArg = 10;
        final Integer expectedResult = 20;

        process = new TestProcess() {
            public Integer receive(Integer arg) {
                called = true;
                assertSame(sendArg, arg);
                return expectedResult;
            }
        };

        assertDispatchSuccess(expectedResult, sendArg);
    }

    public void _testNullArgument() throws Exception {
        process = new TestProcess() {
            public void receive(Integer arg) {
                called = true;
                assertNull(arg);
            }
        };

        Object result = dispatcher.dispatch(process, new Object[]{null});
        assertNull(result);
        process.assertCalled();
    }

    public void _testSuperclassMatch() throws Exception {
        final List arg = new LinkedList();

        process = new TestProcess() {
            public void receive(List e) {
                called = true;
                assertSame(arg, e);
            }

            public void receive(ArrayList e) {
                fail();
            }
        };

        assertVoidDispatchSuccess(arg);
    }
}
