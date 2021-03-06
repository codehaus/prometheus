/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

/**
 * Unittests the {@link org.codehaus.prometheus.references.DefaultAwaitableReference#isTakePossible()} method.
 *
 * @author Peter Veentjer.
 */
public class DefaultAwaitableReference_IsTakePossibleTest extends DefaultAwaitableReference_AbstractTest {

    public void testItemAvailable() {
        Integer ref = 10;

        awaitableRef = new DefaultAwaitableReference<Integer>(ref);
        assertTakeIsPossible();

        //if an item is taken, it doesn't influence the isTakePossible
        spawned_take(ref);
        assertTakeIsPossible();
    }

    public void testNoItemAvailable() {
        awaitableRef = new DefaultAwaitableReference<Integer>();
        assertTakeIsNotPossible();
    }

    private void assertTakeIsPossible() {
        assertTrue(awaitableRef.isTakePossible());
    }

    private void assertTakeIsNotPossible() {
        assertFalse(awaitableRef.isTakePossible());
    }
}
