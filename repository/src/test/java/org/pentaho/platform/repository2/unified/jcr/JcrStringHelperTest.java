/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.repository2.unified.jcr;

import org.junit.Test;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

public class JcrStringHelperTest {
  private static final String PATH_TO_REPORT_NOT_ENCODED = "/home/admin/[~!@#$%^&*(){}|.,]-=_+|;'\"?<>~`.prpti";
  private static final String PATH_TO_REPORT_ENCODED = "/home/admin/%5B~!@#$%25^&%2A(){}%7C.,%5D-=_+%7C;'\"?<>~`.prpti";
  private static final String SOLUTION_PATH = "src/test/resources/solution";

  @Test
  public void testLetterDigitIDEncode() {
    String id = "a243423d-adasdasdasd-asdasd";
    String encodedId = JcrStringHelper.idEncode( id );
    assertEquals( id, encodedId );
  }

  @Test
  public void testDigitLetterIDEncode() {
    String id = "5a243423d-adasdasdasd-asdasd";
    String encodedId = JcrStringHelper.idEncode( id );
    assertEquals( id, encodedId );
  }

  @Test
  public void testFilePath_1EncodeUsingMultiByteEncoding() throws PlatformInitializationException {
    MicroPlatform mp = new MicroPlatform( getSolutionPath() );
    mp.defineInstance( "useMultiByteEncoding", new Boolean( true ) );
    // Start the micro-platform
    mp.start();
    JcrStringHelper.setMultiByteEncodingEnabled( true );
    String path = "/asdf/3err";
    String encodedPath = JcrStringHelper.pathEncode( path );
    assertFalse( path.equals( encodedPath ) );
    assertEquals( "/asdf/_x0033_err", encodedPath );
  }

  @Test
  public void testFilePath_1Encode() throws PlatformInitializationException {
    MicroPlatform mp = new MicroPlatform( getSolutionPath() );
    mp.defineInstance( "useMultiByteEncoding", new Boolean( false ) );
    // Start the micro-platform
    mp.start();
    JcrStringHelper.setMultiByteEncodingEnabled( false );
    String path = "/asdf/3err";
    String encodedPath = JcrStringHelper.pathEncode( path );
    assertEquals( path, encodedPath );
  }

  @Test
  public void testFilePathSpecEncodeDecodeUsingMultiByteEncoding() throws PlatformInitializationException {
    MicroPlatform mp = new MicroPlatform( getSolutionPath() );
    mp.defineInstance( "useMultiByteEncoding", new Boolean( true ) );
    // Start the micro-platform
    mp.start();
    JcrStringHelper.setMultiByteEncodingEnabled( true );

    String path = "/asdf/3err";
    String encodedPath = JcrStringHelper.pathEncode( path );
    assertFalse( path.equals( encodedPath ) );
    String decodedPath = JcrStringHelper.fileNameDecode( encodedPath );
    assertEquals( path, decodedPath );
  }

  @Test
  public void testFilePathSpecEncodeDecode() throws PlatformInitializationException {
    MicroPlatform mp = new MicroPlatform( getSolutionPath() );
    mp.defineInstance( "useMultiByteEncoding", new Boolean( false ) );
    // Start the micro-platform
    mp.start();
    JcrStringHelper.setMultiByteEncodingEnabled( false );

    String path = "/asdf/3err";
    String encodedPath = JcrStringHelper.pathEncode( path );
    assertEquals( path, encodedPath );
    String decodedPath = JcrStringHelper.fileNameDecode( encodedPath );
    assertEquals( path, decodedPath );
  }

  @Test
  public void testFilePathEncode() {
    String path = "/asdf/err";
    String encodedPath = JcrStringHelper.pathEncode( path );
    assertEquals( path, encodedPath );
  }

  @Test
  public void testFilePathEncodeDecode() {
    String path = "/asdf/err";
    String encodedPath = JcrStringHelper.pathEncode( path );
    assertEquals( path, encodedPath );
    String decodedPath = JcrStringHelper.fileNameDecode( encodedPath );
    assertEquals( path, decodedPath );
  }

  @Test
  public void testPathWithSpecificCharactersDecode() {
    String encodedPath = JcrStringHelper.pathEncode( PATH_TO_REPORT_NOT_ENCODED );
    assertFalse( PATH_TO_REPORT_NOT_ENCODED.equals( encodedPath ) );
    String decodedPath = JcrStringHelper.pathDecode( encodedPath );
    assertEquals( PATH_TO_REPORT_NOT_ENCODED, decodedPath );
  }

  @Test
  public void testFileIsNotEncoded() {
    assertFalse( JcrStringHelper.isEncoded( PATH_TO_REPORT_NOT_ENCODED ) );
  }

  @Test
  public void testFileIsEncoded() {
    assertTrue( JcrStringHelper.isEncoded( PATH_TO_REPORT_ENCODED ) );
  }

  protected String getSolutionPath() {
    return SOLUTION_PATH;
  }

}
