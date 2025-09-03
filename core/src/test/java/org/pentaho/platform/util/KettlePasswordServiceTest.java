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


package org.pentaho.platform.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class KettlePasswordServiceTest {

  private KettlePasswordService service;

  @Before
  public void setup() {
    service = new KettlePasswordService();
  }

  @Test
  public void testEncryptNullPassword( ) throws Exception {
    String res = service.encrypt( null );
    assertNull( res );
  }

  @Test
  public void testEncryptWhiteSpacePassword( ) throws Exception{
    String res = service.encrypt( "   " );
    assertEquals( "Encrypted 2be98afc86aa7f2e4cb79ce10bed2ef9a", res );
  }

  @Test
  public void testEncryptEmptyPassword( ) throws Exception{
    String res = service.encrypt( "" );
    assertEquals( "", res );
  }

  @Test
  public void testEncryptValidPassword( ) throws Exception {
    String password = "asdfghjkldieukancmvaiodfweewjrkwer";
    String res = service.encrypt( password );
    assertEquals( res, "Encrypted 6173646667686a6b6c646965756b616e636fc8f9c6a70ec18581ae0ea462d585aac8" );
    assertEquals( password, service.decrypt( res ) );
  }

  @Test
  public void testDecryptWhiteSpacePasswordFallbackToBase64( ) throws Exception {
    String res = service.decrypt( "ICAg" );
    assertEquals( "   ", res );
  }

  @Test
  public void testDecryptEmptyPassword( ) throws Exception {
    String res = service.decrypt( "" );
    assertEquals( "", res );
  }

  @Test
  public void testDecryptNullPassword( ) throws Exception {
    String res = service.decrypt( null );
    assertNull( res );
  }

  @Test
  public void testDecryptValidPassword( ) throws Exception {
    String res = service.decrypt( "Encrypted 6173646667686a6b6c646965756b616e636fc8f9c6a70ec18581ae0ea462d585aac8" );
    assertEquals( "asdfghjkldieukancmvaiodfweewjrkwer", res );
  }

  @Test
  public void testEncryptDecryptPasswordSpecialCharacters( ) throws Exception {
    String toEncode = "AbC@#$%^&*()[]\"'`~\t\r\n";
    String encodedPw = service.encrypt( toEncode );
    String decodedPw = service.decrypt( encodedPw );
    assertEquals( toEncode, decodedPw );
  }

}
