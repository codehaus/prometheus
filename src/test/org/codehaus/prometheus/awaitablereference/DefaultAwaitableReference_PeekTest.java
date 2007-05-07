package org.codehaus.prometheus.awaitablereference;

/**
 * Unittests the {@link DefaultAwaitableReference#peek()} method.
 */
public class DefaultAwaitableReference_PeekTest extends DefaultAwaitableReference_AbstractTests{

    public void test(){
        awaitableRef = new DefaultAwaitableReference<Integer>();
        assertNull(awaitableRef.peek());

        Integer newRef = 20;

        put(newRef);
        assertSame(newRef,awaitableRef.peek());

        put(null);
        assertNull(awaitableRef.peek());
    }

    private void put(Integer newRef) {
        PutThread putThread = schedulePut(newRef);
        joinAll(putThread);
        putThread.assertIsTerminatedWithoutThrowing();
    }
}
