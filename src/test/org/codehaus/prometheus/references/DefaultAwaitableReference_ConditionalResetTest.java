/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

/**
 * Unittests {@link DefaultAwaitableReference#conditionalReset(Object)}.
 *
 * @author Peter Veentjer.
 */
public class DefaultAwaitableReference_ConditionalResetTest extends DefaultAwaitableReference_AbstractTest{

    public void testDifferentValue(){
        Integer ref = 10;
        Integer otherRef = 20;
        awaitableRef = new DefaultAwaitableReference<Integer>(ref);

        spawned_conditionalReset(otherRef);
        assertHasReference(ref);
    }

    public void testSameInstance(){
        Integer ref = 10;
        awaitableRef = new DefaultAwaitableReference<Integer>(ref);

        spawned_conditionalReset(ref);
        assertHasReference(null);
    }

    public void testEqualValue(){
        Integer ref1 = 10;
        Integer ref2 = 10;
        awaitableRef = new DefaultAwaitableReference<Integer>(ref1);

        spawned_conditionalReset(ref2);
        assertHasReference(null);
    }
}
