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


package org.pentaho.platform.plugin.services.importexport;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ExportFileNameEncoderTest {

  @Test
  public void testEncodeZipFileName() {
    assertEquals( "", ExportFileNameEncoder.encodeZipFileName( "" ) );
    assertEquals( "simpleFileName.extention", ExportFileNameEncoder.encodeZipFileName( "simpleFileName.extention" ) );
    assertEquals( "two+words", ExportFileNameEncoder.encodeZipFileName( "two words" ) );
    assertEquals( "special+characters%3A%28%2B%29%25%5E", ExportFileNameEncoder.encodeZipFileName( "special characters:(+)%^" ) );
  }

  @Test
  public void testEncodeZipPathName() {
    assertEquals( "", ExportFileNameEncoder.encodeZipPathName( "" ) );
    assertEquals( "", ExportFileNameEncoder.encodeZipPathName( "/" ) );
    assertEquals( ".", ExportFileNameEncoder.encodeZipPathName( "." ) );
    assertEquals( "./..", ExportFileNameEncoder.encodeZipPathName( "./.." ) );
    assertEquals( "root.dir/mean+dir/leaf+dir_specials%3A%28%2B%29%25%5E", ExportFileNameEncoder.encodeZipPathName( "root.dir/mean dir/leaf dir_specials:(+)%^" ) );
    assertEquals( "/root.dir/mean+dir/leaf+dir_specials%3A%28%2B%29%25%5E", ExportFileNameEncoder.encodeZipPathName( "/root.dir/mean dir/leaf dir_specials:(+)%^" ) );
    assertEquals( "root.dir/mean+dir/leaf+dir_specials%3A%28%2B%29%25%5E/", ExportFileNameEncoder.encodeZipPathName( "root.dir/mean dir/leaf dir_specials:(+)%^/" ) );
    assertEquals( "/root.dir/mean+dir/leaf+dir_specials%3A%28%2B%29%25%5E/", ExportFileNameEncoder.encodeZipPathName( "/root.dir/mean dir/leaf dir_specials:(+)%^/" ) );
  }

  @Test
  public void testDecodeZipFileName() {
    assertEquals( "", ExportFileNameEncoder.decodeZipFileName( "" ) );
    assertEquals( "simpleFileName.extention", ExportFileNameEncoder.decodeZipFileName( "simpleFileName.extention" ) );
    assertEquals( "two words", ExportFileNameEncoder.decodeZipFileName( "two+words" ) );
    assertEquals( "special characters:(+)%^", ExportFileNameEncoder.decodeZipFileName( "special+characters%3A%28%2B%29%25%5E" ) );
  }

  @Test
  public void testDecodeZipPath() {
    assertEquals( "", ExportFileNameEncoder.decodeZipPath( "" ) );
    assertEquals( "", ExportFileNameEncoder.decodeZipPath( "/" ) );
    assertEquals( ".", ExportFileNameEncoder.decodeZipPath( "." ) );
    assertEquals( "./..", ExportFileNameEncoder.decodeZipPath( "./.." ) );
    assertEquals( "root.dir/mean dir/leaf dir_specials:(+)%^", ExportFileNameEncoder.decodeZipPath( "root.dir/mean+dir/leaf+dir_specials%3A%28%2B%29%25%5E" ) );
    assertEquals( "/root.dir/mean dir/leaf dir_specials:(+)%^", ExportFileNameEncoder.decodeZipPath( "/root.dir/mean+dir/leaf+dir_specials%3A%28%2B%29%25%5E" ) );
    assertEquals( "root.dir/mean dir/leaf dir_specials:(+)%^/", ExportFileNameEncoder.decodeZipPath( "root.dir/mean+dir/leaf+dir_specials%3A%28%2B%29%25%5E/" ) );
    assertEquals( "/root.dir/mean dir/leaf dir_specials:(+)%^/", ExportFileNameEncoder.decodeZipPath( "/root.dir/mean+dir/leaf+dir_specials%3A%28%2B%29%25%5E/" ) );
  }
}
