/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import junit.framework.TestSuite;

/**
 * It should test that it doesn't happen that multiple references
 * are lend.
 *
 * takeback and takebackandreset should be tested
 *
 * @author Peter Veentjer.
 */
public class StrictLendableReference_StressTest {

    public static TestSuite suite(){
        return new TestSuite();
    }
}
