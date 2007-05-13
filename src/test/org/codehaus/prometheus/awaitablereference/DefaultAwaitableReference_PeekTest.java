package org.codehaus.prometheus.awaitablereference;

import org.codehaus.prometheus.testsupport.InterruptedTrueFalse;
import org.codehaus.prometheus.references.DefaultAwaitableReference;

/**
 * Unittests the {@link org.codehaus.prometheus.references.DefaultAwaitableReference#peek()} method.
 */
public class DefaultAwaitableReference_PeekTest extends DefaultAwaitableReference_AbstractTests{

    @InterruptedTrueFalse
    public void test(boolean startInterrupted){
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
