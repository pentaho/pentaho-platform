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


package org.pentaho.platform.util;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class TestAll {

  /**
   * 
   */
  public static Test suite() {
    return new TestSuite();
  }

  private static TestSuite testAll() {
    TestSuite suite = new TestSuite( "Platform Util" ); //$NON-NLS-1$

    suite.addTestSuite( CleanXmlHelperTest.class );
    suite.addTestSuite( ColorHelperTest.class );
    suite.addTestSuite( DateMathTest.class );
    suite.addTestSuite( InputStreamDataSourceTest.class );
    suite.addTestSuite( LogTest.class );
    suite.addTestSuite( MessageUtilTest.class );
    suite.addTestSuite( ParameterHelperTest.class );
    suite.addTestSuite( PasswordServiceTest.class );
    suite.addTestSuite( PentahoSystemExceptionTest.class );
    suite.addTestSuite( VersionHelperTest.class );
    suite.addTestSuite( XmlHelperTest.class );

    return suite;
  }

  public static void main( final String[] args ) {
    TestRunner.run( TestAll.testAll() );
  }
}
