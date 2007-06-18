package org.codehaus.prometheus.processors.standardprocessor;

import junit.framework.TestCase;

/**
 * Unit tests {@link PropagatePolicy}.
 *
 * @author Peter Veentjer.
 */
public class PropagatePolicyTest extends TestCase {

    public void testHandle() throws Exception {
        PropagatePolicy policy = new PropagatePolicy();
        Exception ex = new Exception();

        try{
            policy.handle(ex,1);
            fail();
        }catch(Exception foundEx){
            assertSame(ex,foundEx);
        }
    }
}
