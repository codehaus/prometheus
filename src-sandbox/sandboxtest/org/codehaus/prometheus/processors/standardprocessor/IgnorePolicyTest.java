package org.codehaus.prometheus.processors.standardprocessor;

import junit.framework.TestCase;
import org.codehaus.prometheus.processors.VoidValue;

/**
 * Unit tests {@link IgnorePolicy}.
 *
 * @author Peter Veentjer.
 */
public class IgnorePolicyTest extends TestCase {

    public void testHandle_oneArgument(){
        assertHandleReturnsVoid(10);
        assertHandleReturnsVoid(VoidValue.INSTANCE);
    }

    public void assertHandleReturnsVoid(Object... in){
        IgnorePolicy ignorePolicy = new IgnorePolicy();
        Object out = ignorePolicy.handle(new Exception(),in);
        assertEquals(VoidValue.INSTANCE,out);
    }

    public void testHandle_noArgument(){
        assertHandleReturnsVoid();
    }

    public void testHandle_multipleArguments(){
        //todo
    }
}
