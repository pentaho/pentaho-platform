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
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.util.oauth.PentahoOAuthUtility;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@RunWith( MockitoJUnitRunner.class )
public class PentahoOAuthAuthorizationRequestRedirectFilterTest {

  @Mock
  private ClientRegistrationRepository clientRegistrationRepository;

  @Mock
  private OAuth2AuthorizationRequestResolver authorizationRequestResolver;

  @Mock
  private HttpServletRequest servletRequest;

  @Mock
  private HttpServletResponse servletResponse;

  @Mock
  private FilterChain filterChain;

  @Test
  public void proceedsWithSuperFilterWhenOAuthEnabledAndNotUserNamePasswordAuthentication() throws Exception {
    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabled ).thenReturn( true );
      pentahoOAuthUtility.when( () -> PentahoOAuthUtility.isUserNamePasswordAuthentication( servletRequest ) )
        .thenReturn( false );

      PentahoOAuthAuthorizationRequestRedirectFilter filter =
        new PentahoOAuthAuthorizationRequestRedirectFilter( clientRegistrationRepository );
      filter.doFilterInternal( servletRequest, servletResponse, filterChain );

      verify( filterChain ).doFilter( servletRequest, servletResponse );
    }
  }

  @Test
  public void skipsSuperFilterWhenOAuthDisabled() throws Exception {
    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabled ).thenReturn( false );

      PentahoOAuthAuthorizationRequestRedirectFilter filter =
        new PentahoOAuthAuthorizationRequestRedirectFilter( clientRegistrationRepository );
      filter.doFilterInternal( servletRequest, servletResponse, filterChain );

      verify( filterChain ).doFilter( servletRequest, servletResponse );
    }
  }

  @Test
  public void skipsSuperFilterWhenUserNamePasswordAuthentication() throws Exception {
    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabled ).thenReturn( true );
      pentahoOAuthUtility.when( () -> PentahoOAuthUtility.isUserNamePasswordAuthentication( servletRequest ) )
        .thenReturn( true );

      PentahoOAuthAuthorizationRequestRedirectFilter filter =
        new PentahoOAuthAuthorizationRequestRedirectFilter( clientRegistrationRepository );
      filter.doFilterInternal( servletRequest, servletResponse, filterChain );

      verify( filterChain ).doFilter( servletRequest, servletResponse );
    }
  }

  @Test
  public void proceedsWithSuperFilterWhenUsingAuthorizationRequestBaseUriConstructor() throws Exception {
    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabled ).thenReturn( true );
      pentahoOAuthUtility.when( () -> PentahoOAuthUtility.isUserNamePasswordAuthentication( servletRequest ) )
        .thenReturn( false );

      PentahoOAuthAuthorizationRequestRedirectFilter filter =
        new PentahoOAuthAuthorizationRequestRedirectFilter( clientRegistrationRepository, "/custom-uri" );
      filter.doFilterInternal( servletRequest, servletResponse, filterChain );

      verify( filterChain ).doFilter( servletRequest, servletResponse );
    }
  }

  @Test
  public void proceedsWithSuperFilterWhenUsingAuthorizationRequestResolverConstructor() throws Exception {
    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabled ).thenReturn( true );
      pentahoOAuthUtility.when( () -> PentahoOAuthUtility.isUserNamePasswordAuthentication( servletRequest ) )
        .thenReturn( false );

      PentahoOAuthAuthorizationRequestRedirectFilter filter =
        new PentahoOAuthAuthorizationRequestRedirectFilter( authorizationRequestResolver );
      filter.doFilterInternal( servletRequest, servletResponse, filterChain );

      verify( filterChain ).doFilter( servletRequest, servletResponse );
    }
  }
}
