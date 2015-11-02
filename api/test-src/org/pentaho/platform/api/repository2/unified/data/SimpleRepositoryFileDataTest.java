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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.repository2.unified.data;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bgroves on 10/30/15.
 */
public class SimpleRepositoryFileDataTest {
  private static final String INPUT_STRING = "Example String";
  private static final InputStream INPUT_STREAM = new ByteArrayInputStream( INPUT_STRING.getBytes(
    StandardCharsets.UTF_8 ) );
  private static final String ENCODING = "UTF-8";
  private static final String MIME_TYPE = "text/plain";

  private SimpleRepositoryFileData file;
  private InputStream inputStreamSpy;

  @Before
  public void setUp() {
    inputStreamSpy = spy( INPUT_STREAM );
    file = new SimpleRepositoryFileData( inputStreamSpy, ENCODING, MIME_TYPE );
  }

  @Test
  public void testFileData() {
    assertEquals( inputStreamSpy, file.getStream() );
    assertEquals( inputStreamSpy, file.getInputStream() );
    assertEquals( ENCODING, file.getEncoding() );
    assertEquals( MIME_TYPE, file.getMimeType() );

    file.getDataSize();
    try {
      verify( inputStreamSpy ).available();
    } catch ( Exception e ) {
      fail( "No exception should be thrown on the verify" );
    }

    assertNotNull( file.toString() );
    verify( inputStreamSpy ).markSupported();
    verify( inputStreamSpy ).mark( Integer.MAX_VALUE );
    try {
      verify( inputStreamSpy ).reset();
    } catch ( Exception e ) {
      fail( "No exception should be thrown." );
    }
  }

  @Test
  public void testNoEncoding() {
    file = new SimpleRepositoryFileData( inputStreamSpy, "", MIME_TYPE );
    assertNotNull( file.toString() );
    verify( inputStreamSpy ).markSupported();
    verify( inputStreamSpy ).mark( Integer.MAX_VALUE );
    try {
      verify( inputStreamSpy, atLeastOnce() ).read( any( byte[].class ) );
      verify( inputStreamSpy ).reset();
    } catch ( Exception e ) {
      fail( "No exception should be thrown." );
    }
  }

  @Test
  public void testExceptions() {
    InputStream stream = mock( InputStream.class );
    try {
      doThrow( new IOException() ).when( stream ).available();
    } catch ( Exception e ) {
      fail( "available Exception test failed" );
    }
    file = new SimpleRepositoryFileData( stream, "", MIME_TYPE );
    long size = file.getDataSize();
    assertEquals( 0, size );
  }

  @Test
  public void testNotSupported() {
    when( inputStreamSpy.markSupported() ).thenReturn( false );
    assertTrue( file.toString().contains( "unable to show" ) );
  }

  @Test
  public void testLongExcept() {
    String longExcerpt = "this is a really long string that exceeds the max length of 20 hopefully!";
    InputStream inputStream = new ByteArrayInputStream( longExcerpt.getBytes(
      StandardCharsets.UTF_8 ) );
    inputStreamSpy = spy( inputStream );
    file = new SimpleRepositoryFileData( inputStreamSpy, ENCODING, MIME_TYPE );
    String theString = file.toString();
    assertTrue( theString.contains( "..." ) );

    file = new SimpleRepositoryFileData( inputStreamSpy, "", MIME_TYPE );
    theString = file.toString();
    assertTrue( theString.contains( "..." ) );
  }
}
