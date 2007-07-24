package org.codehaus.prometheus.references;

/**
 * Unittests the {@link org.codehaus.prometheus.references.DefaultAwaitableReference#peek()} method.
 */
public class DefaultAwaitableReference_PeekTest extends DefaultAwaitableReference_AbstractTest {

    public void test() {
        awaitableRef = new DefaultAwaitableReference<Integer>();

        //check that the initial null value is seen
        assertNull(awaitableRef.peek());

        //check when a new value is set, the new value is seen
        Integer ref = 20;
        spawned_put(ref, null);
        assertSame(ref, awaitableRef.peek());

        //check if restored to null, the null value is seen
        spawned_put(null, ref);
        assertNull(awaitableRef.peek());
    }
}
