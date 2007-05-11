package org.codehaus.prometheus.awaitablereference;

/**
 * Unittests the {@link DefaultAwaitableReference#peek()} method.
 */
public class DefaultAwaitableReference_PeekTest extends DefaultAwaitableReference_AbstractTests{

    public void test(){
        awaitableRef = new DefaultAwaitableReference<Integer>();

        //check that the initial null value is seen
        assertNull(awaitableRef.peek());
       
        //check when a new value is set, the new value is seen
        Integer ref = 20;
        put(ref,null);
        assertSame(ref,awaitableRef.peek());

        //check if restored to null, the null value is seen
        put(null,ref);
        assertNull(awaitableRef.peek());
    }
}
