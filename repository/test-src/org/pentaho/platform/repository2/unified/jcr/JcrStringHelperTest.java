/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.jcr;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.mt.ITenantManager;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.test.platform.engine.core.MicroPlatform;

public class JcrStringHelperTest {

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
    MicroPlatform mp = new MicroPlatform();
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
    MicroPlatform mp = new MicroPlatform();
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
    MicroPlatform mp = new MicroPlatform();
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
    MicroPlatform mp = new MicroPlatform();
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
    String path = "/home/admin/[~!@#$%^&*(){}|.,]-=_+|;'\"?<>~`.prpti";
    String encodedPath = JcrStringHelper.pathEncode( path );
    assertFalse( path.equals( encodedPath ) );
    String decodedPath = JcrStringHelper.pathDecode( encodedPath );
    assertEquals( path, decodedPath );
  }
}
