package org.codehaus.prometheus.processors;

import junit.framework.TestCase;

/**
 * Unittests the {@link StandardEventDispatcher}.
 *
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

    private void dispatchReturnsFalse(Process process) {
        Event event = new DummyEvent();
        try {
            boolean result = dispatcher.dispatch(process, event);
            assertFalse(result);
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
        Process process = new Process() {
            public void foo() {
            }
        };

        dispatchReturnsFalse(process);
    }

    //this test needs to be defined better
    public void testAccessModifier_private() {
        Process process = new Process() {
            private void handle(Event e) {
            }
        };

        dispatchReturnsFalse(process);
    }

    public void testAccessModifier_packageFriendly() {
        Process process = new Process() {
            void handle(Event e) {
            }
        };

        dispatchReturnsFalse(process);
    }

    public void testAccessModifier_protected() {
        Process process = new Process() {
            protected void handle(Event e) {
            }
        };

        dispatchReturnsFalse(process);
    }

    public void testNotEnoughArguments() throws Exception {
        Process process = new Process() {
            public void handle() {
            }
        };

        dispatchReturnsFalse(process);
    }

    public void testTooManyArguments() {
        Process process = new Process() {
            public void handle(Event event1, Event event2) {
            }
        };

        dispatchReturnsFalse(process);
    }

    public void testArgumentIsNotOfCorrectType_completelyDifferentType() {
        Process process = new Process() {
            public void handle(int foo) {
            }
        };

        dispatchReturnsFalse(process);
    }

    public void testArgumentIsNotOfCorrectType_subtype(){        
        //Process process = new Process(){
        //
        //}
    }

    public void testReturntypeIsNotVoid() {

    }

    public void testHandlerThrowsRuntimeException() {

    }

    public void testHandlerThrowsCheckedException() {

    }

    public void testHandlerSuccessfully() {

    }
}
