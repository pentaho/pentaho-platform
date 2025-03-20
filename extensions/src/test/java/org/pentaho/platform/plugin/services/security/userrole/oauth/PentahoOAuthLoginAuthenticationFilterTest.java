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

package org.pentaho.platform.plugin.services.security.userrole.oauth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@RunWith( MockitoJUnitRunner.class )
public class PentahoOAuthLoginAuthenticationFilterTest {

  @Mock
  ClientRegistrationRepository clientRegistrationRepository;

  @Mock
  OAuth2AuthorizedClientService authorizedClientService;

  @Mock
  OAuth2AuthorizedClientRepository authorizedClientRepository;

  @Mock
  IAuthenticationRoleMapper roleMapper;

  @Test
  public void testAttemptAuthenticationWithRepository() {
    PentahoOAuthLoginAuthenticationFilter filter = spy(
      new PentahoOAuthLoginAuthenticationFilter( clientRegistrationRepository, authorizedClientRepository, "/",
        roleMapper ) );
    HttpServletRequest request = mock( HttpServletRequest.class );
    HttpServletResponse response = mock( HttpServletResponse.class );
    Authentication authentication = mock( Authentication.class );

    doReturn( authentication ).when( filter ).attemptAuthentication( request, response );

    Authentication result = filter.attemptAuthentication( request, response );

    assertEquals( authentication, result );
  }

  @Test
  public void testAttemptAuthentication() {
    PentahoOAuthLoginAuthenticationFilter filter = spy(
      new PentahoOAuthLoginAuthenticationFilter( clientRegistrationRepository, authorizedClientService, "/",
        roleMapper ) );
    HttpServletRequest request = mock( HttpServletRequest.class );
    HttpServletResponse response = mock( HttpServletResponse.class );
    Authentication authentication = mock( Authentication.class );

    doReturn( authentication ).when( filter ).attemptAuthentication( request, response );

    Authentication result = filter.attemptAuthentication( request, response );

    assertEquals( authentication, result );
  }
}