package org.codehaus.prometheus.awaitablereference;

/**
 * Unittests the {@link DefaultAwaitableReference#isTakePossible()} method.
 *
 * @author Peter Veentjer.
 */
public class DefaultAwaitableReference_IsTakePossibleTest extends DefaultAwaitableReference_AbstractTests {

    public void testItemAvailable() {
        Integer ref = 10;

        awaitableRef = new DefaultAwaitableReference<Integer>(ref);
        assertTakeIsPossible();

        //taking an item also doesn't influence
        take();
        assertTakeIsPossible();
    }

    public void testNoItemAvailable() {
        awaitableRef = new DefaultAwaitableReference<Integer>();
        assertTakeIsNotPossible();
    }

    private void assertTakeIsPossible() {
        assertTrue(awaitableRef.isTakePossible());
    }

    private void take() {
        TakeThread thread = scheduleTake();
        
        joinAll(thread);
        thread.assertIsTerminatedWithoutThrowing();
    }

    private void assertTakeIsNotPossible() {
        assertFalse(awaitableRef.isTakePossible());
    }
}
