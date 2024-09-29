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
