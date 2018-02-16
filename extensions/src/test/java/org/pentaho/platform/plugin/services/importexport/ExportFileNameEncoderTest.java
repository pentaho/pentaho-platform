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
