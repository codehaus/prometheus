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
        assertTrue(awaitableRef.isTakePossible());

        //check that a take doesn't influence
        TakeThread thread = scheduleTake();
        joinAll(thread);
        assertTrue(awaitableRef.isTakePossible());
    }

    public void testNoItemAvailable() {
        awaitableRef = new DefaultAwaitableReference<Integer>();
        assertFalse(awaitableRef.isTakePossible());
    }
}
