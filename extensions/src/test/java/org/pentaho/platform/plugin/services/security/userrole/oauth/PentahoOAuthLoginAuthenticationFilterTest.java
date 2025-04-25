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
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.security.IAuthenticationRoleMapper;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.oauth.PentahoOAuthUtility;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

  @Mock
  private HttpServletRequest servletRequest;

  @Mock
  private ServletResponse servletResponse;

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

  @Test
  public void testDoFilter() throws IOException, ServletException {
    PentahoOAuthLoginAuthenticationFilter filter =
      new PentahoOAuthLoginAuthenticationFilter( clientRegistrationRepository, authorizedClientService, roleMapper );
    ServletRequest request = mock( ServletRequest.class );
    ServletResponse response = mock( ServletResponse.class );
    FilterChain filterChain = mock( FilterChain.class );

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabled ).thenReturn( false );
      filter.doFilter( request, response, filterChain );
      verify( filterChain ).doFilter( request, response );
    }
  }

  @Test
  public void testDoFilterForOAuthEnabled() {
    PentahoOAuthLoginAuthenticationFilter filter =
      new PentahoOAuthLoginAuthenticationFilter( clientRegistrationRepository, authorizedClientService, roleMapper );
    FilterChain filterChain = mock( FilterChain.class );

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      pentahoOAuthUtility.when(
          () -> PentahoOAuthUtility.isUserNamePasswordAuthentication( servletRequest ) )
        .thenReturn( false );
      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabled ).thenReturn( true );

      assertThrows( ClassCastException.class, () -> {
        filter.doFilter( (ServletRequest) servletRequest, servletResponse, filterChain );
      } );
    }
  }

  @Test
  public void testDoFilterForOAuthEnabledAndBasicAuth() throws ServletException, IOException {
    PentahoOAuthLoginAuthenticationFilter filter =
      new PentahoOAuthLoginAuthenticationFilter( clientRegistrationRepository, authorizedClientService, roleMapper );
    FilterChain filterChain = mock( FilterChain.class );

    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      pentahoOAuthUtility.when(
          () -> PentahoOAuthUtility.isUserNamePasswordAuthentication( servletRequest ) )
        .thenReturn( true );
      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabled ).thenReturn( true );

      filter.doFilter( (ServletRequest) servletRequest, servletResponse, filterChain );

      verify( filterChain ).doFilter( servletRequest, servletResponse );

    }
  }

  @Test
  public void testGetOAuth2AuthenticationToken() {
    DefaultOidcUser defaultOidcUser = mock( DefaultOidcUser.class );
    Authentication authentication = mock( OAuth2AuthenticationToken.class );
    when( ( (OAuth2AuthenticationToken) authentication ).getAuthorizedClientRegistrationId() ).thenReturn( "test" );
    when( authentication.getPrincipal() ).thenReturn( defaultOidcUser );
    when( defaultOidcUser.getAttribute( "roles" ) ).thenReturn( List.of( "Administrator" ) );
    when( roleMapper.toPentahoRole( "Administrator" ) ).thenReturn( "Administrator" );

    Collection<GrantedAuthority> authorities = List.of( new SimpleGrantedAuthority( "Administrator" ) );
    doReturn( authorities ).when( authentication ).getAuthorities();

    try (
      MockedStatic<PentahoSessionHolder> pentahoSessionHolderMockedStatic = mockStatic( PentahoSessionHolder.class );
      MockedStatic<PentahoSystem> mockedPentahoSystemMockedStatic = mockStatic( PentahoSystem.class ) ) {
      pentahoSessionHolderMockedStatic.when( PentahoSessionHolder::getSession )
        .thenReturn( mock( IPentahoSession.class ) );
      mockedPentahoSystemMockedStatic.when(
          () -> PentahoSystem.get( String.class, "singleTenantAuthenticatedAuthorityName", null ) )
        .thenReturn( "Authenticated" );
      PentahoOAuthLoginAuthenticationFilter filter =
        new PentahoOAuthLoginAuthenticationFilter( clientRegistrationRepository, authorizedClientService, roleMapper );
      assertNotNull( filter.getOAuth2AuthenticationToken( authentication ) );
    }
  }

  @Test
  public void testGetOAuth2AuthenticationTokenNoRolesAndAuthorities() {
    DefaultOidcUser defaultOidcUser = mock( DefaultOidcUser.class );
    Authentication authentication = mock( OAuth2AuthenticationToken.class );
    when( ( (OAuth2AuthenticationToken) authentication ).getAuthorizedClientRegistrationId() ).thenReturn( "test" );
    when( authentication.getPrincipal() ).thenReturn( defaultOidcUser );
    when( defaultOidcUser.getAttribute( "roles" ) ).thenReturn( Collections.emptyList() );
    when( authentication.getAuthorities() ).thenReturn( Collections.emptyList() );

    try (
      MockedStatic<PentahoSessionHolder> pentahoSessionHolderMockedStatic = mockStatic( PentahoSessionHolder.class );
      MockedStatic<PentahoSystem> mockedPentahoSystemMockedStatic = mockStatic( PentahoSystem.class ) ) {
      pentahoSessionHolderMockedStatic.when( PentahoSessionHolder::getSession )
        .thenReturn( mock( IPentahoSession.class ) );
      mockedPentahoSystemMockedStatic.when(
          () -> PentahoSystem.get( String.class, "singleTenantAuthenticatedAuthorityName", null ) )
        .thenReturn( "Authenticated" );
      PentahoOAuthLoginAuthenticationFilter filter =
        new PentahoOAuthLoginAuthenticationFilter( clientRegistrationRepository, authorizedClientService, roleMapper );
      assertNotNull( filter.getOAuth2AuthenticationToken( authentication ) );
    }
  }

}
