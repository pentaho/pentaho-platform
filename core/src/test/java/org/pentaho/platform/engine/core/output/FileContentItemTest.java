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
