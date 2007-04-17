/*
 * Copyright (c) 2007 Peter Veentjer
 *
 * This program is made available under the terms of the MIT License.
 */
package org.codehaus.prometheus.util;

import junit.framework.TestCase;
import org.codehaus.prometheus.util.ConcurrencyUtil;

public  class ConcurrencyUtil_TimeoutOccurredTest extends TestCase {

    public void testNegativeTimeout(){
          assertTrue(ConcurrencyUtil.hasTimeoutOccurred(-1));
      }

      public void testNulTimeout(){
          assertFalse(ConcurrencyUtil.hasTimeoutOccurred(0));
      }

      public void testPositiveTimeout(){
          assertFalse(ConcurrencyUtil.hasTimeoutOccurred(1));
      }
  }

