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


package org.pentaho.platform.security.userroledao.jackrabbit;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.repository2.userroledao.jackrabbit.security.DefaultPentahoPasswordEncoder;
import org.pentaho.test.platform.security.userroledao.ws.UserRoleWebServiceBase;

public class DefaultPentahoPasswordEncoderTest {

  @Test
  public void testValidCredentials( ) {
    DefaultPentahoPasswordEncoder passwordEncoder = new DefaultPentahoPasswordEncoder();
    String password = "helloworld";
    String encryptedPassword =  new UserRoleWebServiceBase.PasswordEncoderMock().encode( password );
    Assert.assertTrue( passwordEncoder.isPasswordValid( encryptedPassword, password, null ) );
  }

  @Test
  public void testInvalidCredentials( ) {
    DefaultPentahoPasswordEncoder passwordEncoder = new DefaultPentahoPasswordEncoder( );
    Assert.assertFalse( passwordEncoder.isPasswordValid( "password", null, null ) );
    Assert.assertFalse( passwordEncoder.isPasswordValid( passwordEncoder.encodePassword( "", null ), "password", null ) );
    Assert.assertFalse( passwordEncoder.isPasswordValid( null, null, null ) );
  }
}
