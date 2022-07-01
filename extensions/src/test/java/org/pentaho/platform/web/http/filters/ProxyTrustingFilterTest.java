/*!
 *
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
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.filters;

import com.hitachivantara.security.web.service.csrf.servlet.CsrfValidator;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Callable;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.pentaho.platform.web.http.filters.ProxyTrustingFilter.CSRF_OPERATION_NAME;

public class ProxyTrustingFilterTest {

  private static final String TRUSTED_IP = "127.0.0.1";
  private static final String UNTRUSTED_IP = "8.8.8.8";

  private ISecurityHelper securityHelper;
  private MockHttpServletRequest request;
  private ProxyTrustingFilter filter;

  @Before
  public void setUp() throws Exception {
    securityHelper = mock( ISecurityHelper.class );
    SecurityHelper.setMockInstance( securityHelper );

    request = new MockHttpServletRequest();
  }

  @After
  public void tearDown() {
    SecurityHelper.setMockInstance( null );
  }

  @Test
  public void doFilterForTrusted() throws Exception {
    MockFilterConfig cfg = new MockFilterConfig();
    cfg.addInitParameter( "TrustedIpAddrs", "1.1.1.1," + TRUSTED_IP );

    filter = new ProxyTrustingFilter();
    filter.init( cfg );

    request.setRemoteHost( TRUSTED_IP );
    request.addParameter( filter.getParameterName(), "user" );

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    IPentahoSession session = (IPentahoSession) request.getSession().getAttribute( PentahoSystem.PENTAHO_SESSION_KEY );
    assertNotNull( session );
    assertEquals( "user", session.getName() );
  }

  @Test
  public void doFilterForUntrusted() throws Exception {
    MockFilterConfig cfg = new MockFilterConfig();
    cfg.addInitParameter( "TrustedIpAddrs", "1.1.1.1," + TRUSTED_IP );

    filter = new ProxyTrustingFilter();
    filter.init( cfg );

    request.setRemoteHost( UNTRUSTED_IP );

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    verify( securityHelper, never() ).runAsUser( nullable( String.class ), any( Callable.class ) );
  }

  // region Locale Override
  @Test
  public void doFilterForTrustedWithoutLocaleThenUsesDefaultLocale() throws Exception {
    MockFilterConfig cfg = new MockFilterConfig();
    cfg.addInitParameter( "TrustedIpAddrs", "1.1.1.1," + TRUSTED_IP );

    filter = spy( new ProxyTrustingFilter() );
    filter.init( cfg );

    MockHttpSession httpSession = (MockHttpSession) request.getSession( true );
    assert httpSession != null;

    request.setRemoteHost( TRUSTED_IP );
    request.addParameter( filter.getParameterName(), "user" );

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    verify( filter ).setSystemLocaleOverrideCode( null );

    assertNull( httpSession.getAttribute( IPentahoSession.ATTRIBUTE_LOCALE_OVERRIDE ) );
    assertNull( LocaleHelper.getThreadLocaleOverride() );
  }

  @Test
  public void doFilterForTrustedWithLocaleParameterThenSetsLocaleOverride() throws Exception {
    MockFilterConfig cfg = new MockFilterConfig();
    cfg.addInitParameter( "TrustedIpAddrs", "1.1.1.1," + TRUSTED_IP );

    filter = spy( new ProxyTrustingFilter() );
    filter.init( cfg );

    MockHttpSession httpSession = (MockHttpSession) request.getSession( true );
    assert httpSession != null;

    request.setRemoteHost( TRUSTED_IP );
    request.addParameter( filter.getParameterName(), "user" );
    request.addParameter( filter.getLocaleOverrideParameterName(), "pt_PT" );

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    verify( filter ).setSystemLocaleOverrideCode( "pt_PT" );
    Locale locale = LocaleHelper.getThreadLocaleOverride();
    assertEquals( new Locale( "pt", "PT" ), locale );
    assertEquals( locale.toString(), httpSession.getAttribute( IPentahoSession.ATTRIBUTE_LOCALE_OVERRIDE ) );
  }

  @Test
  public void doFilterForTrustedWithLocaleHeaderThenSetsLocaleOverride() throws Exception {
    MockFilterConfig cfg = new MockFilterConfig();
    cfg.addInitParameter( "TrustedIpAddrs", "1.1.1.1," + TRUSTED_IP );
    cfg.addInitParameter( "CheckHeader", "true" );

    filter = spy( new ProxyTrustingFilter() );
    filter.init( cfg );

    MockHttpSession httpSession = (MockHttpSession) request.getSession( true );
    assert httpSession != null;

    request.setRemoteHost( TRUSTED_IP );
    request.addParameter( filter.getParameterName(), "user" );
    request.addHeader( filter.getLocaleOverrideHeaderName(), "pt_PT" );

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    verify( filter ).setSystemLocaleOverrideCode( "pt_PT" );
    Locale locale = LocaleHelper.getThreadLocaleOverride();
    assertEquals( new Locale( "pt", "PT" ), locale );
    assertEquals( locale.toString(), httpSession.getAttribute( IPentahoSession.ATTRIBUTE_LOCALE_OVERRIDE ) );
  }

  @Test
  public void doFilterForTrustedWithLocaleParameterAndHeaderThenParameterWins() throws Exception {
    MockFilterConfig cfg = new MockFilterConfig();
    cfg.addInitParameter( "TrustedIpAddrs", "1.1.1.1," + TRUSTED_IP );
    cfg.addInitParameter( "CheckHeader", "true" );

    filter = spy( new ProxyTrustingFilter() );
    filter.init( cfg );

    MockHttpSession httpSession = (MockHttpSession) request.getSession( true );
    assert httpSession != null;

    request.setRemoteHost( TRUSTED_IP );
    request.addParameter( filter.getParameterName(), "user" );
    request.addParameter( filter.getLocaleOverrideParameterName(), "pt_PT" );
    request.addHeader( filter.getLocaleOverrideHeaderName(), "pt-BR" );

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    verify( filter ).setSystemLocaleOverrideCode( "pt_PT" );
    Locale locale = LocaleHelper.getThreadLocaleOverride();
    assertEquals( new Locale( "pt", "PT" ), locale );
    assertEquals( locale.toString(), httpSession.getAttribute( IPentahoSession.ATTRIBUTE_LOCALE_OVERRIDE ) );
  }

  @Test
  public void doFilterForTrustedWithCustomLocaleParameter() throws Exception {
    MockFilterConfig cfg = new MockFilterConfig();
    cfg.addInitParameter( "TrustedIpAddrs", "1.1.1.1," + TRUSTED_IP );
    cfg.addInitParameter( "LocaleOverrideParameterName", "LOC_OVERRIDE" );

    filter = spy( new ProxyTrustingFilter() );
    filter.init( cfg );

    MockHttpSession httpSession = (MockHttpSession) request.getSession( true );
    assert httpSession != null;

    request.setRemoteHost( TRUSTED_IP );
    request.addParameter( filter.getParameterName(), "user" );
    request.addParameter( "LOC_OVERRIDE", "pt_PT" );

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    verify( filter ).setSystemLocaleOverrideCode( "pt_PT" );
    Locale locale = LocaleHelper.getThreadLocaleOverride();
    assertEquals( new Locale( "pt", "PT" ), locale );
    assertEquals( locale.toString(), httpSession.getAttribute( IPentahoSession.ATTRIBUTE_LOCALE_OVERRIDE ) );
  }

  @Test
  public void doFilterForTrustedWithCustomLocaleHeader() throws Exception {
    MockFilterConfig cfg = new MockFilterConfig();
    cfg.addInitParameter( "TrustedIpAddrs", "1.1.1.1," + TRUSTED_IP );
    cfg.addInitParameter( "CheckHeader", "true" );
    cfg.addInitParameter( "LocaleOverrideHeaderName", "LOC_OVERRIDE" );

    filter = spy( new ProxyTrustingFilter() );
    filter.init( cfg );

    MockHttpSession httpSession = (MockHttpSession) request.getSession( true );
    assert httpSession != null;

    request.setRemoteHost( TRUSTED_IP );
    request.addParameter( filter.getParameterName(), "user" );
    request.addHeader( "LOC_OVERRIDE", "pt_PT" );

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    verify( filter ).setSystemLocaleOverrideCode( "pt_PT" );
    Locale locale = LocaleHelper.getThreadLocaleOverride();
    assertEquals( new Locale( "pt", "PT" ), locale );
    assertEquals( locale.toString(), httpSession.getAttribute( IPentahoSession.ATTRIBUTE_LOCALE_OVERRIDE ) );
  }

  @Test
  public void testSetCsrfValidationRespectsIt() {

    CsrfValidator csrfValidatorMock = mock( CsrfValidator.class );
    ProxyTrustingFilter filter = new ProxyTrustingFilter(csrfValidatorMock);

    // ---

    assertSame( csrfValidatorMock, filter.getCsrfValidator() );
  }

  private void testWhenCsrfValidationFailsWithGivenExceptionThenRethrows( @NonNull Throwable validateError )
          throws ServletException, IOException {

    CsrfValidator csrfValidatorMock = mock( CsrfValidator.class );

    MockFilterConfig cfg = new MockFilterConfig();
    cfg.addInitParameter( "TrustedIpAddrs", "1.1.1.1," + TRUSTED_IP );

    filter = new ProxyTrustingFilter(csrfValidatorMock);
    filter.init( cfg );

    request.setRemoteHost( TRUSTED_IP );
    request.addParameter( filter.getParameterName(), "user" );

    when( csrfValidatorMock.validateRequestOfMutationOperation( any( HttpServletRequest.class ),
            eq( filter.getClass() ), anyString() ) )
            .thenThrow( validateError );

    // ---

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

  }

  @Test( expected = IOException.class )
  public void testWhenCsrfValidationFailsWithIOExceptionThenRethrowsAsInternalAuthenticationServiceException() throws ServletException, IOException {

    IOException error = mock( IOException.class );

    try {

      testWhenCsrfValidationFailsWithGivenExceptionThenRethrows(error);

      fail( "Should have thrown exception" );
    } catch ( InternalAuthenticationServiceException ex ) {
        assertSame( error, ex.getCause() );
      }
  }

  @Test( expected = ServletException.class )
  public void testWhenCsrfValidationFailsWithServletExceptionThenRethrowsAsInternalAuthenticationServiceException() throws ServletException, IOException {

    ServletException error = mock( ServletException.class );

    try {

      testWhenCsrfValidationFailsWithGivenExceptionThenRethrows(error);

      fail( "Should have thrown exception" );
    } catch ( InternalAuthenticationServiceException ex ) {
        assertSame( error, ex.getCause() );
    }
  }

  @Test
  public void testAttemptAuthenticationWhichThrowsAccessDeniedException()
          throws ServletException, IOException {

    AccessDeniedException error = mock( AccessDeniedException.class );

    HttpServletResponse responseMock = mock( HttpServletResponse.class );

    FilterChain chainMock = mock( FilterChain.class );

    CsrfValidator csrfValidatorMock = mock( CsrfValidator.class );

    MockFilterConfig cfg = new MockFilterConfig();
    cfg.addInitParameter( "TrustedIpAddrs", "1.1.1.1," + TRUSTED_IP );

    filter = new ProxyTrustingFilter( csrfValidatorMock );
    filter.init( cfg );

    request.setRemoteHost( TRUSTED_IP );
    request.addParameter( filter.getParameterName(), "user" );

    when( csrfValidatorMock.validateRequestOfMutationOperation( any( HttpServletRequest.class ),
            eq( filter.getClass() ), anyString() ) )
            .thenThrow( error );

    // ---

    filter.doFilter( request, responseMock, chainMock );

    // ---

    verify( responseMock, times( 1 ) ).sendError( 403 );

    verify( chainMock, times( 0 ) ).doFilter( any( HttpServletRequest.class ),
            any( HttpServletResponse.class ));
  }

  @Test
  public void testCsrfValidationIsCalledWithCorrectOperationId() throws ServletException, IOException {

    CsrfValidator csrfValidatorMock = mock( CsrfValidator.class );

    MockFilterConfig cfg = new MockFilterConfig();
    cfg.addInitParameter( "TrustedIpAddrs", "1.1.1.1," + TRUSTED_IP );

    filter = new ProxyTrustingFilter( csrfValidatorMock );
    filter.init( cfg );

    request.setRemoteHost( TRUSTED_IP );
    request.addParameter( filter.getParameterName(), "user" );

    // ---

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    // ---

    verify( csrfValidatorMock, times( 1 ) )
            .validateRequestOfMutationOperation( any( HttpServletRequest.class ),
                    eq( filter.getClass() ),
                    eq( CSRF_OPERATION_NAME ) );
  }

  @Test
  public void doFilterForTrustedWithCSrfValidation() throws Exception {
    MockFilterConfig cfg = new MockFilterConfig();
    cfg.addInitParameter( "TrustedIpAddrs", "1.1.1.1," + TRUSTED_IP );

    CsrfValidator csrfValidatorMock = mock( CsrfValidator.class );

    FilterChain  chainMock = mock( FilterChain.class );

    filter = new ProxyTrustingFilter( csrfValidatorMock );
    filter.init( cfg );

    request.setRemoteHost( TRUSTED_IP );
    request.addParameter( filter.getParameterName(), "user" );

    filter.doFilter( request, new MockHttpServletResponse(), chainMock );

    IPentahoSession session =
            (IPentahoSession) request.getSession().getAttribute( PentahoSystem.PENTAHO_SESSION_KEY );
    assertNotNull( session );
    assertEquals( "user", session.getName() );

    verify( chainMock,times( 1 ) ).doFilter( any( HttpServletRequest.class ), any( HttpServletResponse.class ) );
  }

  @Test
  public void doFilterForUntrustedWithCSrfValidation() throws Exception {
    MockFilterConfig cfg = new MockFilterConfig();
    cfg.addInitParameter( "TrustedIpAddrs", "1.1.1.1," + TRUSTED_IP );

    CsrfValidator csrfValidatorMock = mock( CsrfValidator.class );

    FilterChain chainMock = mock( FilterChain.class );

    filter = new ProxyTrustingFilter( csrfValidatorMock );
    filter.init( cfg );

    request.setRemoteHost( UNTRUSTED_IP );

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    //---

    verify( securityHelper, never() ).runAsUser( nullable( String.class ), any( Callable.class ) );

    verify( chainMock, times( 0 ) ).doFilter( any( HttpServletRequest.class ), any( HttpServletResponse.class ) );
  }

  // endregion
}
