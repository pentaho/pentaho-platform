/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.servlet;

import static org.junit.Assert.assertFalse;
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
    assertTrue( testUtils.checkExtension( "", false ) );
    testUtils.setAllowsNoExtension( false );
    assertFalse( testUtils.checkExtension( "", false ) );
    testUtils.setAllowsNoExtension( true );
    assertTrue( testUtils.checkExtension( "", false ) );
  }
}
