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

package org.pentaho.platform.core.mimetype;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.repository2.unified.Converter;

import static org.junit.Assert.*;

import static org.mockito.Mockito.mock;

public class MimeTypeTest {
  public static final String MIME_NAME = "mimeName";
  public static final String MIME_NAME2 = "ABC";
  public static final String[] EXTENSION_LIST1 = new String[] { "ext1", "ext2" };
  public static final List<String> EXTENSIONS1 = Arrays.asList( EXTENSION_LIST1 );
  public static MimeType mimeType;

  @Before
  public void setup() {
    mimeType = new MimeType();
  }

  @Test
  public void ConstructorWithExtensionListTest() {
    mimeType = new MimeType( MIME_NAME, EXTENSIONS1 );
    mimeType.setName( MIME_NAME );
    assertEquals( MIME_NAME, mimeType.getName() );
  }

  @Test
  public void ConstructorWithExtensionStringTest() {
    mimeType = new MimeType( MIME_NAME, EXTENSION_LIST1.toString() );
    mimeType.setName( MIME_NAME );
    assertEquals( MIME_NAME, mimeType.getName() );
  }

  @Test
  public void setAndGetNameTest() {
    mimeType.setName( MIME_NAME );
    assertEquals( MIME_NAME, mimeType.getName() );
  }

  @Test
  public void setAndGetConverterTest() {
    Converter conv = mock( Converter.class );
    mimeType.setConverter( conv );
    assertEquals( conv, mimeType.getConverter() );
  }

  @Test
  public void setAndGetExtensionsTest() {
    mimeType.setExtensions( EXTENSIONS1 );
    validateExtensionList();
  }

  @Test
  public void setAndGetHiddenTest() {
    assertEquals( false, mimeType.isHidden() );
    mimeType.setHidden( true );
    assertEquals( true, mimeType.isHidden() );
  }

  private void validateExtensionList() {
    List<String> extList = mimeType.getExtensions();
    assertEquals( EXTENSIONS1, extList );
  }

  @Test
  public void setAndGetLocaleTest() {
    assertEquals( false, mimeType.isLocale() );
    mimeType.setLocale( true );
    assertEquals( true, mimeType.isLocale() );
  }

  @Test
  public void setAndGetVersionCommentEnabledTest() {
    assertEquals( false, mimeType.isVersionCommentEnabled() );
    mimeType.setVersionCommentEnabled( true );
    assertEquals( true, mimeType.isVersionCommentEnabled() );
  }

  @Test
  public void setAndGetVersionEnabledTest() {
    assertEquals( false, mimeType.isVersionEnabled() );
    mimeType.setVersionEnabled( true );
    assertEquals( true, mimeType.isVersionEnabled() );
  }

  @Test
  public void toStringTest() {
    mimeType = new MimeType( MIME_NAME, EXTENSIONS1 );
    String s = mimeType.toString();
    assertNotNull( s );
    assert ( s.contains( "name:" ) );
    assertTrue( s.contains( "extensions:" ) );
  }

  @Test
  public void CompareTest() {
    mimeType = new MimeType( MIME_NAME, EXTENSIONS1 );
    MimeType mimeType2 = new MimeType( MIME_NAME2, EXTENSIONS1 );
    assertTrue( mimeType.compareTo( mimeType2 ) > 0 );
    assertTrue( mimeType2.compareTo( mimeType ) < 0 );
    assertEquals( 0, mimeType2.compareTo( mimeType2 ) );
  }

  @Test
  public void hashTest() {
    assertEquals( mimeType.hashCode(), mimeType.hashCode() );
    mimeType = new MimeType( MIME_NAME, EXTENSIONS1 );
    MimeType mimeType2 = new MimeType( MIME_NAME, EXTENSIONS1 );
    assertEquals( mimeType.hashCode(), mimeType2.hashCode() );
  }

  @Test
  public void equalTest() {
    MimeType mimeType2 = new MimeType();
    assertTrue( mimeType.equals( mimeType2 ) );
    mimeType = new MimeType( MIME_NAME, EXTENSIONS1 );
    assertFalse( mimeType.equals( mimeType2 ) );
    assertFalse( mimeType2.equals( mimeType ) );
    mimeType2 = new MimeType( MIME_NAME2, EXTENSIONS1 );
    assertTrue( mimeType.equals( mimeType ) );
    assertFalse( mimeType2.equals( mimeType ) );
    mimeType2 = new MimeType( MIME_NAME, EXTENSIONS1 );
    assertTrue( mimeType.equals( mimeType2 ) );
    assertFalse( mimeType.equals( null ) );
    assertFalse( mimeType.equals( "abc" ) );
  }
}
