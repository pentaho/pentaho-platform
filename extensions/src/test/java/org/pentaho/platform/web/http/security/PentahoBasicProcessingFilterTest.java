/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2022 Hitachi Vantara. All rights reserved.
 *
 */
package org.pentaho.platform.web.http.security;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SessionCookieConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.web.http.security.PentahoBasicProcessingFilter.SESSION_FLUSHED_COOKIE_NAME;
import static org.pentaho.platform.web.http.security.PentahoBasicProcessingFilter.SESSION_ID_COOKIE_NAME;

public class PentahoBasicProcessingFilterTest {

  private static boolean COOKIE_IS_HTTP_ONLY_NOT_DEFAULT = true;
  private static boolean COOKIE_IS_SECURE_NOT_DEFAULT = true;
  private static String CONTEXT_PATH = "/pentaho";

  private AuthenticationManager authenticationManagerMock;
  private AuthenticationEntryPoint authenticationEntryPointMock;

  private SessionCookieConfig sessionCookieConfigMock;

  private HttpServletRequest requestMock;

  private SecurityContext previousSecurityContext;

  private SecurityContext securityContextMock;

  // region Helpers
  private HttpServletRequest configureRequestMockWithInvalidRequestedSessionId(
    @NonNull HttpServletRequest requestMock ) {
    when( requestMock.getRequestedSessionId() ).thenReturn( "SESSION_ID_1_INVALID" );
    when( requestMock.isRequestedSessionIdValid() ).thenReturn( false );

    return requestMock;
  }

  private HttpServletRequest configureRequestMockWithBasicHttpAuthPresent( @NonNull HttpServletRequest requestMock ) {

    when( requestMock.getHeader( "Authorization" ) ).thenReturn( "Basic base64_encoded_credentials" );

    return requestMock;
  }

  private Cookie createSessionFlushedCookieMock() {

    // A submitted cookie only comes with this information.
    Cookie sessionFlushedCookieMock = mock( Cookie.class );
    when( sessionFlushedCookieMock.getName() ).thenReturn( SESSION_FLUSHED_COOKIE_NAME );
    when( sessionFlushedCookieMock.getValue() ).thenReturn( "true" );

    return sessionFlushedCookieMock;
  }

  private Cookie createSessionIdCookieMock() {

    // A submitted cookie only comes with this information.
    Cookie sessionFlushedCookieMock = mock( Cookie.class );
    when( sessionFlushedCookieMock.getName() ).thenReturn( SESSION_ID_COOKIE_NAME );
    when( sessionFlushedCookieMock.getValue() ).thenReturn( "session_id_1" );

    return sessionFlushedCookieMock;
  }

  private PentahoBasicProcessingFilter createBasicProcessingFilter() {
    return new PentahoBasicProcessingFilter( authenticationManagerMock, authenticationEntryPointMock );
  }

  private void verifySessionFlushedCookieMockWasConfigured( @NonNull Cookie cookieMock, int maxAge ) {
    verify( cookieMock, times( 1 ) ).setHttpOnly( COOKIE_IS_HTTP_ONLY_NOT_DEFAULT );
    verify( cookieMock, times( 1 ) ).setSecure( COOKIE_IS_SECURE_NOT_DEFAULT );
    verify( cookieMock, times( 1 ) ).setPath( CONTEXT_PATH );

    verify( cookieMock, times( 1 ) ).setMaxAge( maxAge );
  }

  private void verifySessionFlushedCookieConfiguration( @NonNull Cookie cookie, int maxAge ) {
    assertEquals( COOKIE_IS_HTTP_ONLY_NOT_DEFAULT, cookie.isHttpOnly() );
    assertEquals( COOKIE_IS_SECURE_NOT_DEFAULT, cookie.getSecure() );
    assertEquals( CONTEXT_PATH, cookie.getPath() );
    assertEquals( maxAge, cookie.getMaxAge() );
  }
  // endregion

  @Before
  public void setUp() {
    authenticationManagerMock = mock( AuthenticationManager.class );
    authenticationEntryPointMock = mock( AuthenticationEntryPoint.class );

    sessionCookieConfigMock = mock( SessionCookieConfig.class );
    when( sessionCookieConfigMock.isHttpOnly() ).thenReturn( COOKIE_IS_HTTP_ONLY_NOT_DEFAULT );
    when( sessionCookieConfigMock.isSecure() ).thenReturn( COOKIE_IS_SECURE_NOT_DEFAULT );

    ServletContext servletContextMock = mock( ServletContext.class );
    when( servletContextMock.getSessionCookieConfig() ).thenReturn( sessionCookieConfigMock );

    requestMock = mock( HttpServletRequest.class );
    when( requestMock.getDispatcherType() ).thenReturn( DispatcherType.REQUEST );
    when( requestMock.getContextPath() ).thenReturn( CONTEXT_PATH );
    when( requestMock.getServletContext() ).thenReturn( servletContextMock );

    securityContextMock = mock( SecurityContext.class );
    when( securityContextMock.getAuthentication() ).thenReturn( null );

    previousSecurityContext = SecurityContextHolder.getContext();
    SecurityContextHolder.setContext( securityContextMock );
  }

  @After
  public void tearDown() {
    SecurityContextHolder.setContext( previousSecurityContext );
  }

  // region When RequestedSessionId is invalid

  // region And BasicAuth is Present
  @Test
  public void testWhenSessionIdInvalidAndBasicAuthPresentAndSessionFlushedCookiePresentThenConfiguresAndClearsIt()
    throws ServletException, IOException {

    configureRequestMockWithInvalidRequestedSessionId( requestMock );
    configureRequestMockWithBasicHttpAuthPresent( requestMock );

    Cookie sessionFlushedCookieMock = createSessionFlushedCookieMock();
    when( requestMock.getCookies() ).thenReturn( new Cookie[] { sessionFlushedCookieMock } );

    HttpServletResponse responseMock = mock( HttpServletResponse.class );
    FilterChain filterChainMock = mock( FilterChain.class );

    PentahoBasicProcessingFilter filter = createBasicProcessingFilter();

    // ---

    filter.doFilter( requestMock, responseMock, filterChainMock );

    // ---

    verify( responseMock, times( 1 ) ).addCookie( sessionFlushedCookieMock );

    verifySessionFlushedCookieMockWasConfigured( sessionFlushedCookieMock, 0 );
  }

  @Test
  public void testWhenSessionIdInvalidAndBasicAuthPresentThenDelegatesToAuthenticationEntryPoint()
    throws ServletException, IOException {

    configureRequestMockWithInvalidRequestedSessionId( requestMock );
    configureRequestMockWithBasicHttpAuthPresent( requestMock );

    HttpServletResponse responseMock = mock( HttpServletResponse.class );
    FilterChain filterChainMock = mock( FilterChain.class );

    PentahoBasicProcessingFilter filterSpy = spy( createBasicProcessingFilter() );

    // ---

    filterSpy.doFilter( requestMock, responseMock, filterChainMock );

    // ---

    verify( authenticationEntryPointMock, times( 1 ) )
      .commence( eq( requestMock ), eq( responseMock ), any() );

    verify( filterSpy, times( 0 ) )
      .doFilterInternalSuper( requestMock, responseMock, filterChainMock );
  }
  // endregion

  // region And BasicAuth is Not Present
  @Test
  public void testWhenSessionIdInvalidAndBasicAuthNotPresentAndHasSessionIdCookieThenCreatesAndConfiguresSessionFlushedCookie()
    throws ServletException, IOException {

    configureRequestMockWithInvalidRequestedSessionId( requestMock );

    Cookie sessionIdCookieMock = createSessionIdCookieMock();
    when( requestMock.getCookies() ).thenReturn( new Cookie[] { sessionIdCookieMock } );

    HttpServletResponse responseMock = mock( HttpServletResponse.class );
    FilterChain filterChainMock = mock( FilterChain.class );

    PentahoBasicProcessingFilter filter = createBasicProcessingFilter();

    // ---

    filter.doFilter( requestMock, responseMock, filterChainMock );

    // ---

    ArgumentCaptor<Cookie> sessionFlushedCookieCaptor = ArgumentCaptor.forClass( Cookie.class );
    verify( responseMock, times( 1 ) )
      .addCookie( sessionFlushedCookieCaptor.capture() );

    Cookie sessionFlushedCookie = sessionFlushedCookieCaptor.getValue();
    verifySessionFlushedCookieConfiguration( sessionFlushedCookie, -1 );
  }

  @Test
  public void testWhenSessionIdInvalidAndBasicAuthNotPresentAndHasSessionIdCookieThenProceedsToBaseClass()
    throws ServletException, IOException {

    configureRequestMockWithInvalidRequestedSessionId( requestMock );

    Cookie sessionIdCookieMock = createSessionIdCookieMock();
    when( requestMock.getCookies() ).thenReturn( new Cookie[] { sessionIdCookieMock } );

    HttpServletResponse responseMock = mock( HttpServletResponse.class );
    FilterChain filterChainMock = mock( FilterChain.class );

    PentahoBasicProcessingFilter filterSpy = spy( createBasicProcessingFilter() );

    // ---

    filterSpy.doFilter( requestMock, responseMock, filterChainMock );

    // ---

    verify( filterSpy, times( 1 ) )
      .doFilterInternalSuper( requestMock, responseMock, filterChainMock );
  }
  // endregion

  // endregion

  // region When RequestedSessionId Is valid

  // region And BasicAuth is Present And Session is Not Authenticated
  @Test
  public void testWhenSessionIdValidAndBasicAuthPresentAndSessionIsNotAuthenticatedAndSessionFlushedIsPresentThenConfiguresAndClearsIt()
    throws ServletException, IOException {

    configureRequestMockWithBasicHttpAuthPresent( requestMock );

    Cookie sessionFlushedCookieMock = createSessionFlushedCookieMock();
    when( requestMock.getCookies() ).thenReturn( new Cookie[] { sessionFlushedCookieMock } );

    HttpServletResponse responseMock = mock( HttpServletResponse.class );
    FilterChain filterChainMock = mock( FilterChain.class );

    PentahoBasicProcessingFilter filter = createBasicProcessingFilter();

    // ---

    filter.doFilter( requestMock, responseMock, filterChainMock );

    // ---

    verify( responseMock, times( 1 ) ).addCookie( sessionFlushedCookieMock );

    verifySessionFlushedCookieMockWasConfigured( sessionFlushedCookieMock, 0 );
  }

  @Test
  public void testWhenSessionIdValidAndBasicAuthPresentAndSessionIsNotAuthenticatedAndSessionFlushedIsPresentThenDelegatesToAuthenticationEntryPoint()
    throws ServletException, IOException {

    configureRequestMockWithBasicHttpAuthPresent( requestMock );

    Cookie sessionFlushedCookieMock = createSessionFlushedCookieMock();
    when( requestMock.getCookies() ).thenReturn( new Cookie[] { sessionFlushedCookieMock } );

    HttpServletResponse responseMock = mock( HttpServletResponse.class );
    FilterChain filterChainMock = mock( FilterChain.class );

    PentahoBasicProcessingFilter filterSpy = spy( createBasicProcessingFilter() );

    // ---

    filterSpy.doFilter( requestMock, responseMock, filterChainMock );

    // ---

    verify( authenticationEntryPointMock, times( 1 ) )
      .commence( eq( requestMock ), eq( responseMock ), any() );

    verify( filterSpy, times( 0 ) )
      .doFilterInternalSuper( requestMock, responseMock, filterChainMock );
  }

  @Test
  public void testWhenSessionIdValidAndBasicAuthIsPresentAndSessionIsAuthenticatedThenProceedsToBaseClass()
    throws ServletException, IOException {

    configureRequestMockWithBasicHttpAuthPresent( requestMock );

    Authentication authenticationMock = mock( Authentication.class );
    when( securityContextMock.getAuthentication() ).thenReturn( authenticationMock );

    HttpServletResponse responseMock = mock( HttpServletResponse.class );
    FilterChain filterChainMock = mock( FilterChain.class );

    PentahoBasicProcessingFilter filterSpy = spy( createBasicProcessingFilter() );

    // ---

    filterSpy.doFilter( requestMock, responseMock, filterChainMock );

    // ---

    verify( filterSpy, times( 1 ) )
      .doFilterInternalSuper( requestMock, responseMock, filterChainMock );
  }

  @Test
  public void testWhenSessionIdValidAndBasicAuthIsNotPresentAndSessionIsNotAuthenticatedThenProceedsToBaseClass()
    throws ServletException, IOException {

    HttpServletResponse responseMock = mock( HttpServletResponse.class );
    FilterChain filterChainMock = mock( FilterChain.class );

    PentahoBasicProcessingFilter filterSpy = spy( createBasicProcessingFilter() );

    // ---

    filterSpy.doFilter( requestMock, responseMock, filterChainMock );

    // ---

    verify( filterSpy, times( 1 ) )
      .doFilterInternalSuper( requestMock, responseMock, filterChainMock );
  }

  @Test
  public void testWhenSessionIdValidAndBasicAuthIsNotPresentAndSessionIsAuthenticatedThenProceedsToBaseClass()
    throws ServletException, IOException {

    Authentication authenticationMock = mock( Authentication.class );
    when( securityContextMock.getAuthentication() ).thenReturn( authenticationMock );

    HttpServletResponse responseMock = mock( HttpServletResponse.class );
    FilterChain filterChainMock = mock( FilterChain.class );

    PentahoBasicProcessingFilter filterSpy = spy( createBasicProcessingFilter() );

    // ---

    filterSpy.doFilter( requestMock, responseMock, filterChainMock );

    // ---

    verify( filterSpy, times( 1 ) )
      .doFilterInternalSuper( requestMock, responseMock, filterChainMock );
  }
  // endregion

  // endregion
}
