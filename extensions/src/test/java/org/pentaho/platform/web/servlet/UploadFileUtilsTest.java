/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.web.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;

public class UploadFileUtilsTest {

  @Before
  public void setupPentahoSystem() {
    PentahoSystem.setApplicationContext(
      new StandaloneApplicationContext( "test-src/web-servlet-solution/FileUploadTest/Test1", "" ) );
  }

  @Test
  public void testCheckExtensions() throws Exception {
    UploadFileUtils testUtils = new UploadFileUtils( null );
    assertFalse( testUtils.checkExtension( null, false ) );
    assertFalse( testUtils.checkExtension( "exe", false ) );
    assertFalse( testUtils.checkExtension( "app", false ) );
    assertFalse( testUtils.checkExtension( "html", false ) );
    assertTrue( testUtils.checkExtension( "txt", false ) );
    assertTrue( testUtils.checkExtension( "zip", false ) );
    assertTrue( testUtils.checkExtension( "gzip", false ) );
    assertTrue( testUtils.checkExtension( "gz", false ) );
    assertTrue( testUtils.checkExtension( "dat", false ) );
    assertTrue( testUtils.checkExtension( "csv", false ) );
    assertTrue( testUtils.checkExtension( "tar", false ) );
    assertFalse( testUtils.checkExtension( "tar.gz", false ) );
    assertTrue( testUtils.checkExtension( "tgz", false ) );
    // As of PPP-4675, the default value of allowNoExtensions is false,
    // so we will test the default (false), then the explicit value of false
    assertFalse( testUtils.checkExtension( "", false ) );
    testUtils.setAllowsNoExtension( false );
    assertFalse( testUtils.checkExtension( "", false ) );
    testUtils.setAllowsNoExtension( true );
    assertTrue( testUtils.checkExtension( "", false ) );
  }

  @Test
  public void testGetAllExtensions() throws Exception {
    UploadFileUtils testUtils = new UploadFileUtils( null );
    assertNull( testUtils.removeFileName( null ) );
    assertEquals( "", testUtils.removeFileName( "" ) );
    assertEquals( "", testUtils.removeFileName( "aaaa" ) );
    assertEquals( "", testUtils.removeFileName( " bb " ) );
    assertEquals( "a", testUtils.removeFileName( "b.a" ) );
    assertEquals( "b.a", testUtils.removeFileName( "c.b.a" ) );
    assertEquals( "c.b.a", testUtils.removeFileName( "d.c.b.a" ) );
    // BACKLOG-29017: Special characters
    assertEquals( "_________-__________-_____test.csv",
      testUtils.removeFileName( "TestCSVspecialchar.$&#%^( )!- ;[ ]{ }`@-_+=',test.csv" ) );
    assertEquals( "Tes____CSVspecialchar._________-__________-_____test.dat",
      testUtils.removeFileName( "_.Tes~ºª_CSVspecialchar.$&#%^( )!- ;[ ]{ }`@-_+=',test.dat" ) );
  }
}
