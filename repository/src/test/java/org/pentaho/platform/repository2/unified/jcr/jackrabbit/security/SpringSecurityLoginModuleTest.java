/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.repository2.unified.jcr.jackrabbit.security;

import org.junit.Test;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import java.security.Principal;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by nbaker on 8/5/15.
 */
public class SpringSecurityLoginModuleTest {


  Credentials credentials = new SimpleCredentials( "joe", "password".toCharArray() );
  SpringSecurityLoginModule loginModule = new SpringSecurityLoginModule();

  /**
   * Verifies that the LoginModule will respond as expected even if the Spring Security AuthenticationManager is not
   * present.
   */
  @Test
  public void testNullAuthenticationManager() throws Exception {

    SecurityContextHolder.clearContext();

    org.apache.jackrabbit.core.security.authentication.Authentication authentication =
        loginModule.getAuthentication( mock( Principal.class ), credentials );

    assertFalse( authentication.authenticate( credentials ) );


    // Now verify that when the AuthenticationManager does become available it will be used and function.

    Authentication springAuth = mock( Authentication.class );

    AuthenticationManager authenticationManager = mock( AuthenticationManager.class );
    when( authenticationManager.authenticate( any( Authentication.class ) ) ).thenReturn( springAuth );

    org.apache.jackrabbit.core.security.authentication.Authentication mockAuthentication =
        mock( org.apache.jackrabbit.core.security.authentication.Authentication.class );
    when( mockAuthentication.authenticate( credentials ) ).thenReturn( Boolean.TRUE );

    PentahoSystem.registerObject( authenticationManager );
    PentahoSystem.init();

    org.apache.jackrabbit.core.security.authentication.Authentication jackrabbitAuth =
        loginModule.getAuthentication( mock( Principal.class ), credentials );

    assertTrue( jackrabbitAuth.authenticate( credentials ) );
  }


}
