package org.codehaus.prometheus.processors;

import junit.framework.TestCase;

import javax.management.AttributeList;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Unittests the {@link StandardProcessDispatcher}.
 *
 * @author Peter Veentjer.
 */
public class StandardProcessDispatcherTest extends TestCase {
    private TestProcess process;
    private StandardProcessDispatcher dispatcher;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dispatcher = new StandardProcessDispatcher();
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

    //=============== receive throws exceptions ===========================

     private void assertReceiveCausesInvocationTargetException(Exception ex) throws Exception {
         try {
             dispatcher.dispatch(process, VoidValue.INSTANCE);
             fail();
         } catch (InvocationTargetException ite) {
             assertSame(ex, ite.getCause());
         }
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



    //======================= basic matching ============================

    public void testExactMatch_noArg() throws Exception {
        process = new TestProcess() {

            public void receive() {
                signalCalled();
            }

            public void receive(Object o){
                fail();
            }
        };

        assertVoidReceiveIsSuccess(VoidValue.INSTANCE);
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

    public void testNoMatchingName() {
        process = new TestProcess() {
            public void foo(Integer e) {
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

    // =================== access modifiers ==============================

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

     public void testAccessModifier_public() {
         final Object arg = 10;
         process = new TestProcess() {
             public void receive(Integer e) {
                 assertSame(arg, e);
                 signalCalled();
             }
         };

         assertVoidReceiveIsSuccess(10);
     }

    //=========== subtype matching

    public void testNoTypeMatch() {
        process = new TestProcess() {

            public void handle(int foo) {
                signalCalled();
            }
        };

        assertDispatchNoSuchMethod("hello");
    }

    public void testSupertypes_objectFound() throws Exception {
        final Object sendArg = new ArrayList();

        process = new TestProcess() {
            public void receive(Object arg) {
                signalCalled();
                assertSame(sendArg, arg);
            }

            public void receive(AttributeList arg) {
                fail();
            }
        };

        assertVoidReceiveIsSuccess(sendArg);
    }

    public void testSupertypes_parentClassFound() throws Exception {
        final Object sendArg = new ArrayList();

        process = new TestProcess() {
            public void receive(Object arg) {
                fail();
            }

            public void receive(AbstractList arg) {
                signalCalled();
                assertSame(sendArg, arg);
            }

            public void receive(AttributeList arg) {
                fail();
            }
        };

        assertVoidReceiveIsSuccess(sendArg);
    }

    public void testSupertypes_exactmatchFound() throws Exception {
        final Object sendArg = new ArrayList();

        process = new TestProcess() {
            public void receive(Object arg) {
                fail();
            }

            public void receive(ArrayList arg) {
                signalCalled();
                assertSame(sendArg, arg);
            }

            //attributelist is a subclass from arraylist
            public void receive(AttributeList arg) {
                fail();
            }
        };

        assertVoidReceiveIsSuccess(sendArg);
    }

    //=====================================================


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

    public void testReturnValue() throws Exception {
        Integer sendArg = 10;
        Integer expectedResult = 20;

        process = new IntegerProcess(sendArg, expectedResult);

        assertReceiveIsSuccess(expectedResult, sendArg);
    }

    public void testSuperclassMatch_fromArrayListToObject() throws Exception {
        final Object arg = new ArrayList();

        process = new TestProcess() {
            public void receive(Object e) {
                signalCalled();
                assertSame(arg, e);
            }

            public void receive(Vector e) {
                fail();
            }

            public void receive(LinkedList e) {
                fail();
            }
        };

        assertVoidReceiveIsSuccess(arg);
    }

    public void testSuperClassMatch() throws Exception {
        final Object arg = new Vector();

        process = new TestProcess() {
            public void receive(Object e) {
                fail();
            }

            public void receive(AbstractList e) {
                signalCalled();
                assertSame(arg, e);
            }
        };

        assertVoidReceiveIsSuccess(arg);
    }


    public void _testMatchOnInterface() {
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

    public void _testMultipleMatchingInterfaces(){
    }

    public void _testMatchingInterfacesAndClasses(){
        
    }
}
