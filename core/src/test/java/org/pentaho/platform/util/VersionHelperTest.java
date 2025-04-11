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

import junit.framework.Assert;
import junit.framework.TestCase;

public class VersionHelperTest extends TestCase {

  public void testVersionHelper() {

    VersionHelper vh = new VersionHelper();
    String verInfo = vh.getVersionInformation();
    String verInfo2 = vh.getVersionInformation( this.getClass() );

    System.out.println( "Version Info   : " + verInfo ); //$NON-NLS-1$ 
    System.out.println( "Version Info 2 : " + verInfo2 ); //$NON-NLS-1$ 

    Assert.assertTrue( true );

  }

  public static void main( final String[] args ) {
    VersionHelperTest test = new VersionHelperTest();
    try {
      test.testVersionHelper();
    } finally {
    }
  }

}
