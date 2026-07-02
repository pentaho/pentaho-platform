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
