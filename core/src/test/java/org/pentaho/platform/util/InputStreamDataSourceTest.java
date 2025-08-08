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
import org.pentaho.commons.connection.InputStreamDataSource;

import java.io.InputStream;

public class InputStreamDataSourceTest extends TestCase {

  public void testInputStreamDataSource() {
    try {
      InputStream inputStream = getClass().getResourceAsStream( "/test/xml/query_without_connection.xaction" ); //$NON-NLS-1$"test/xml/departments.rule.xaction")); 
      String name = "mystream"; //$NON-NLS-1$
      InputStreamDataSource is = new InputStreamDataSource( name, inputStream );

      Assert.assertEquals( inputStream, is.getInputStream() );
      Assert.assertEquals( name, is.getName() );
    } catch ( Exception e ) {
      e.printStackTrace();
    }

  }
  /*
   * public static void main(String[] args) { InputStreamDataSourceTest test = new InputStreamDataSourceTest();
   * test.testInputStreamDataSource(); try {
   * 
   * } finally { } }
   */
}
