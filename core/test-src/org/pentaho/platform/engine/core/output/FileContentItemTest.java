/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2016 Pentaho Corporation.  All rights reserved.
 */


package org.pentaho.platform.engine.core.output;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileContentItemTest {
  static File tempOutFile;
  FileContentItem fci;

  @BeforeClass
  public static void beforeClass() throws IOException {
    tempOutFile = File.createTempFile( "FileContentItemTest", "out" );
    tempOutFile.delete();
  }

  @Before
  public void setup() {
    Assert.assertFalse( "tempOutFile exists", tempOutFile.exists() );
  }

  @After
  public void tearDown() throws IOException {
    if ( fci != null ) {
      fci.closeOutputStream();
      fci = null;
    }
    tempOutFile.delete();
  }

  @Test
  public void testInitProperties() throws IOException {
    fci = new FileContentItem( tempOutFile );
    Assert.assertNull( "mimeType", fci.getMimeType() );
    Assert.assertNull( "dataSource", fci.getDataSource() );
    Assert.assertNull( "inputStream", fci.getInputStream() );
    Assert.assertNotNull( "outputStream", fci.getOutputStream( null ) );
    Assert.assertEquals( "file", tempOutFile, fci.getFile() );
  }

  @Test
  public void testInitOutputStream() throws IOException {
    fci = new FileContentItem( tempOutFile );
    writeStringToOutputStream( fci.getOutputStream( null ), "asdf" );
    Assert.assertEquals( "content-after", "asdf", FileUtils.readFileToString( tempOutFile ) );
  }

  private void writeStringToOutputStream( OutputStream os, String str ) throws IOException {
    Writer w = null;
    try {
      w = new OutputStreamWriter( os );
      w.write( str );
    } finally {
      if ( w != null ) {
        w.close();
      }
    }
  }

}
