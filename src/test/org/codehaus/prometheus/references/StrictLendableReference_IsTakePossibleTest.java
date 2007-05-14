package org.codehaus.prometheus.references;

/**
 * Unittests the {@link StrictLendableReference#isTakePossible()}.
 *
 * @author Peter Veentjer.
 */
public class StrictLendableReference_IsTakePossibleTest extends StrictLendableReference_AbstractTest<Integer> {

    public void testItemAvailable() {
        Integer ref = 10;
        lendableRef = new StrictLendableReference<Integer>(ref);
        assertTrue(lendableRef.isTakePossible());
    }

   public void testNoItemAvailable() {
        Integer ref = null;
        lendableRef = new StrictLendableReference<Integer>(ref);
        assertFalse(lendableRef.isTakePossible());
    }
}
