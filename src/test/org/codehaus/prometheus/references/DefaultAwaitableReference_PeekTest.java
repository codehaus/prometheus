package org.codehaus.prometheus.awaitablereference;

/**
 * Unittests the {@link DefaultAwaitableReference#peek()} method.
 */
public class DefaultAwaitableReference_PeekTest extends DefaultAwaitableReference_AbstractTests{

    public void test(){
        awaitableRef = new DefaultAwaitableReference<Integer>();
        assertNull(awaitableRef.peek());

        Integer newRef = 20;
        Thread putThread = schedulePut(newRef);
        joinAll(putThread);
        assertSame(newRef,awaitableRef.peek());
    }
}
