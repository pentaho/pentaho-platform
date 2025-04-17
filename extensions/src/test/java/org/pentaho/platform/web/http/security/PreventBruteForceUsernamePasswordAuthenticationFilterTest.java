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

package org.pentaho.platform.web.http.security;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.platform.api.security.ILoginAttemptService;
import org.pentaho.platform.engine.security.LoginAttemptService;
import org.pentaho.platform.util.oauth.PentahoOAuthUtility;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class PreventBruteForceUsernamePasswordAuthenticationFilterTest {

  private ILoginAttemptService mockLoginAttemptService;
  private HttpServletRequest mockRequest;
  private HttpServletResponse mockResponse;
  private AuthenticationManager mockAuthenticationManager;
  private Authentication mockAuthentication;
  private String ip;

  @Before
  public void setUp() {
    mockLoginAttemptService = mock( LoginAttemptService.class );
    mockRequest = mock( HttpServletRequest.class );
    mockResponse = mock( HttpServletResponse.class );
    mockAuthenticationManager = mock( AuthenticationManager.class );
    mockAuthentication = mock( Authentication.class );
    ip = "192.168.0.25";
    when( mockRequest.getHeader( "X-Forwarded-For" ) ).thenReturn( null );
    when( mockRequest.getRemoteAddr() ).thenReturn( ip );
    when( mockRequest.getMethod() ).thenReturn( "POST" );
  }


  @Test
  public void testAttemptAuthenticationWithoutBlock() {
    PreventBruteForceUsernamePasswordAuthenticationFilter authenticationFilter =
      spy( new PreventBruteForceUsernamePasswordAuthenticationFilter( mockLoginAttemptService ) );

    when( mockLoginAttemptService.isBlocked( ip ) ).thenReturn( false );
    when( mockAuthenticationManager.authenticate( any() ) ).thenReturn( mockAuthentication );

    authenticationFilter.setAuthenticationManager( mockAuthenticationManager );

    authenticationFilter.attemptAuthentication( mockRequest, mockResponse );
    verify( mockLoginAttemptService, times( 1 ) ).isBlocked( ip );

  }

  @Test( expected = PreventBruteForceException.class )
  public void testAttemptAuthenticationWithBlock() {
    PreventBruteForceUsernamePasswordAuthenticationFilter authenticationFilter =
      spy( new PreventBruteForceUsernamePasswordAuthenticationFilter( mockLoginAttemptService ) );

    when( mockLoginAttemptService.isBlocked( ip ) ).thenReturn( true );
    authenticationFilter.attemptAuthentication( mockRequest, mockResponse );
  }

  @Test
  public void testAttemptAuthenticationForClientIp() {
    PreventBruteForceUsernamePasswordAuthenticationFilter authenticationFilter =
      new PreventBruteForceUsernamePasswordAuthenticationFilter( mockLoginAttemptService );
    mockAuthentication = mock( OAuth2AuthenticationToken.class );
    when( mockRequest.getSession() ).thenReturn( mock( HttpSession.class ) );
    when( mockRequest.getHeader( "X-Forwarded-For" ) ).thenReturn( "Test,Best" );

    authenticationFilter.setAuthenticationManager( mockAuthenticationManager );

    authenticationFilter.attemptAuthentication( mockRequest, mockResponse );
    verify( mockLoginAttemptService, times( 0 ) ).loginSucceeded( ip );
  }

  @Test
  public void testDoFilterChain() throws ServletException, IOException {
    PreventBruteForceUsernamePasswordAuthenticationFilter authenticationFilter =
      new PreventBruteForceUsernamePasswordAuthenticationFilter( mockLoginAttemptService );
    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabled ).thenReturn( true );
      pentahoOAuthUtility.when( () -> PentahoOAuthUtility.isUserNamePasswordAuthentication( mockRequest ) )
        .thenReturn( false );
      authenticationFilter.doFilter( mockRequest, mockResponse, mock( FilterChain.class ) );
      verify( mockLoginAttemptService, times( 0 ) ).loginSucceeded( ip );
    }
  }

  @Test
  public void testDoFilterApplyThis() throws ServletException, IOException {
    PreventBruteForceUsernamePasswordAuthenticationFilter authenticationFilter =
      new PreventBruteForceUsernamePasswordAuthenticationFilter( mockLoginAttemptService );
    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabled ).thenReturn( false );
      pentahoOAuthUtility.when( () -> PentahoOAuthUtility.isUserNamePasswordAuthentication( mockRequest ) )
        .thenReturn( true );
      authenticationFilter.doFilter( mockRequest, mockResponse, mock( FilterChain.class ) );
      verify( mockLoginAttemptService, times( 0 ) ).loginSucceeded( ip );
    }
  }

  @Test
  public void testDoFilterForOAuth() throws ServletException, IOException {
    PreventBruteForceUsernamePasswordAuthenticationFilter authenticationFilter =
      new PreventBruteForceUsernamePasswordAuthenticationFilter( mockLoginAttemptService );
    try ( MockedStatic<PentahoOAuthUtility> pentahoOAuthUtility = mockStatic( PentahoOAuthUtility.class ) ) {
      pentahoOAuthUtility.when( PentahoOAuthUtility::isOAuthEnabled ).thenReturn( true );
      pentahoOAuthUtility.when( () -> PentahoOAuthUtility.isUserNamePasswordAuthentication( mockRequest ) )
        .thenReturn( true );
      authenticationFilter.doFilter( mockRequest, mockResponse, mock( FilterChain.class ) );
      verify( mockLoginAttemptService, times( 0 ) ).loginSucceeded( ip );
    }
  }

}
