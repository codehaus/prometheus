/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.references;

import junit.framework.TestSuite;

/**
 * Case that needs to be checked is that is it possible to have multiple
 * references lend at the same time.
 *
 * @author Peter Veentjer.
 */
public class RelaxedLendableReferenceStressTest {

    public static TestSuite suite(){
        return new TestSuite();
    }
}
