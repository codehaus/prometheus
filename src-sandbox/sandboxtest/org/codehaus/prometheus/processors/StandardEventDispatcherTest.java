package org.codehaus.prometheus.processors;

import junit.framework.TestCase;

/**
 * Unittests the {@link StandardEventDispatcher}.
 * <p/>
 * todo: checks need to be improved to make sure that a process its handler is not called when it isn't accepted
 *
 * @author Peter Veentjer.
 */
public class StandardEventDispatcherTest extends TestCase {
    private StandardEventDispatcher dispatcher;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dispatcher = new StandardEventDispatcher();
    }

    private void dispatchReturnsFalse(TestProcess process) {
        Event event = new DummyEvent();
        try {
            boolean result = dispatcher.dispatch(process, event);
            assertFalse(result);
            process.assertNotCalled();
        } catch (Exception e) {
            fail();
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
        TestProcess process = new TestProcess() {
            public void foo(Event e) {
                called = true;
            }
        };

        dispatchReturnsFalse(process);
    }

    //this test needs to be defined better
    public void testAccessModifier_private() {
        TestProcess process = new TestProcess() {
            private void handle(Event e) {
                called = true;
            }
        };

        dispatchReturnsFalse(process);
    }

    public void testAccessModifier_packageFriendly() {
        TestProcess process = new TestProcess() {
            void handle(Event e) {
                called = true;
            }
        };

        dispatchReturnsFalse(process);
    }

    public void testAccessModifier_protected() {
        TestProcess process = new TestProcess() {
            protected void handle(Event e) {
                called = true;
            }
        };

        dispatchReturnsFalse(process);
    }

    public void testNotEnoughArguments() throws Exception {
        TestProcess process = new TestProcess() {
            public void handle() {
                called = true;
            }
        };

        dispatchReturnsFalse(process);
    }

    public void testTooManyArguments() {
        TestProcess process = new TestProcess() {
            public void handle(Event event1, Event event2) {
                called = true;
            }
        };

        dispatchReturnsFalse(process);
    }

    public void testArgumentIsNotOfCorrectType_completelyDifferentType() {
        TestProcess process = new TestProcess() {
            public void handle(int foo) {
                called = true;
            }
        };

        dispatchReturnsFalse(process);
    }

    public void testArgumentIsNotOfCorrectType_subtype() {
        //todo
    }

    public void testReturntypeIsNotVoid() {
        TestProcess process = new TestProcess() {
            public String handle(Event event1, Event event2) {
                called = true;
                return null;
            }
        };

        dispatchReturnsFalse(process);
    }

    public void testHandlerThrowsRuntimeException() {
        //todo
    }

    public void testHandlerThrowsCheckedException() {
        //todo
    }

    public void testHandlerSuccessfully_exactMatch() throws Exception {
        final DummyEvent sendEvent = new DummyEvent();

        TestProcess process = new TestProcess() {
            public void handle(DummyEvent e) {
                called = true;
                assertSame(sendEvent, e);
            }
        };

        boolean result = dispatcher.dispatch(process, sendEvent);
        assertTrue(result);
        process.assertCalled();
    }

     public void testHandlerSuccessfully_superclassMatch() throws Exception {
        final DummyEvent sendEvent = new DummyEvent();

        TestProcess process = new TestProcess() {
            public void handle(Event e) {
                called = true;
                assertSame(sendEvent, e);
            }
        };

        boolean result = dispatcher.dispatch(process, sendEvent);
        assertTrue(result);
        process.assertCalled();
    }
}
