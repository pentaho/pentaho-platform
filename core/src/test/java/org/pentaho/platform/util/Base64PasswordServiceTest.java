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


public class Base64PasswordServiceTest {
  private Base64PasswordService service;
  private String res;

  @Before
  public void setUp( ) {
    service = new Base64PasswordService();
  }

  @Test
  public void testEncryptNullPassword( ) throws Exception {
    res = service.encrypt( null );
    assertNull( res );
  }

  @Test
  public void testEncryptWhiteSpacePassword( ) throws Exception{
    res = service.encrypt( "   " );
    assertEquals( res, "ICAg" );
  }

  @Test
  public void testEncryptEmptyPassword( ) throws Exception{
    res = service.encrypt( "" );
    assertEquals( res, "" );
  }

  @Test
  public void testEncryptValidPassword( ) throws Exception {
    String password = "asdfghjkldieukancmvaiodfweewjrkwer";
    res = service.encrypt( password );
    assertEquals( res, "YXNkZmdoamtsZGlldWthbmNtdmFpb2Rmd2Vld2pya3dlcg==" );
    assertEquals( password, service.decrypt( res ) );
  }

  @Test
  public void testDecryptWhiteSpacePassword( ) throws Exception {
    res = service.decrypt( "ICAg" );
    assertEquals( res, "   " );
  }

  @Test
  public void testDecryptEmptyPassword( ) throws Exception {
    res = service.decrypt( "" );
    assertEquals( res, "" );
  }

  @Test
  public void testDecryptNullPassword( ) throws Exception {
    res = service.decrypt( null );
    assertNull( res );
  }

  @Test
  public void testDecryptValidPassword( ) throws Exception {
    res = service.decrypt( "aGVsbG93b3JsZDEyMzEyIyMkJEAoKCko" );
    assertEquals( res, "helloworld12312##$$@(()(" );
  }
}
