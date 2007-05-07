package org.codehaus.prometheus.lendablereference;

public class StrictLendableReference_IsTakePossibleTest extends StrictLendableReference_AbstractTest<Integer> {

    public void testItemAvailable_startUninterrupted() {
        testItemAvailable(START_UNINTERRUPTED);
    }

    public void testItemAvailable_startInterrupted() {
        testItemAvailable(START_INTERRUPTED);
    }

    public void testItemAvailable(boolean startInterrupted) {
        Integer ref = 10;
        lendableRef = new StrictLendableReference<Integer>(ref);
        assertTrue(lendableRef.isTakePossible());
    }

    public void testNoItemAvailable_startUninterrupted() {
        testNoItemAvailable(START_UNINTERRUPTED);
    }

    public void testNoItemAvailable_startInterrupted() {
        testNoItemAvailable(START_INTERRUPTED);
    }

    public void testNoItemAvailable(boolean startInterrupted) {
        Integer ref = null;
        lendableRef = new StrictLendableReference<Integer>(ref);
        assertFalse(lendableRef.isTakePossible());
    }
}
