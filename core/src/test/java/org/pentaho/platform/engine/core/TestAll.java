/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.engine.core;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class TestAll {

  /**
   * 
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    return suite;
  }

  public static TestSuite testAll() {
    TestSuite suite = new TestSuite( "BI Engine Test suite" ); //$NON-NLS-1$

    // suite.addTestSuite(SettingsParameterProviderTest.class);
    // suite.addTestSuite(SimpleParameterProviderTest.class);
    suite.addTestSuite( SimpleUrlTest.class );

    return suite;
  }

  public static void main( final String[] args ) {
    TestRunner.run( TestAll.testAll() );
  }
}
