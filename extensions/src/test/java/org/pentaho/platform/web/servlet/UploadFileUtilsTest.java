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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;

public class UploadFileUtilsTest {

  @Before
  public void setupPentahoSystem() {
    PentahoSystem.setApplicationContext( new StandaloneApplicationContext( "test-src/web-servlet-solution/FileUploadTest/Test1", "" ) );
  }

  @Test
  public void testCheckExtensions() throws Exception {
    UploadFileUtils testUtils = new UploadFileUtils( null );
    assertFalse( testUtils.checkExtension( "", false ) );
    assertFalse( testUtils.checkExtension( null, false ) );
    assertFalse( testUtils.checkExtension( "test.exe", false ) );
    assertFalse( testUtils.checkExtension( "test.app", false ) );
    assertFalse( testUtils.checkExtension( "first.csv.html", false ) );
    assertTrue( testUtils.checkExtension( "junk.csv.exe.txt", false ) );
    assertFalse( testUtils.checkExtension( "test.txt.", false ) );
    assertTrue( testUtils.checkExtension( "test.zip", false ) );
    assertTrue( testUtils.checkExtension( "test.gzip", false ) );
    assertTrue( testUtils.checkExtension( "test.csv.gz", false ) );
    assertTrue( testUtils.checkExtension( "test.dat", false ) );
    assertTrue( testUtils.checkExtension( "test.csv", false ) );
    assertTrue( testUtils.checkExtension( "test.tar", false ) );
    assertTrue( testUtils.checkExtension( "test.tar.gz", false ) );
    assertTrue( testUtils.checkExtension( "test.tgz", false ) );
    assertTrue( testUtils.checkExtension( "test", false ) );
    testUtils.setAllowsNoExtension( false );
    assertFalse( testUtils.checkExtension( "test", false ) );
  }

}
