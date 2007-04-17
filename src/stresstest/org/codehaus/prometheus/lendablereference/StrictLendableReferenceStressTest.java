/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.lendablereference;

import junit.framework.TestSuite;

/**
 * It should test that it doesn't happen that multiple references
 * are lend.
 *
 */
public class StrictLendableReferenceStressTest {

    public static TestSuite suite(){
        return new TestSuite();
    }
}
