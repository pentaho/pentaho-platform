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

import java.util.Locale;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

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

    IPentahoSession session =
      (IPentahoSession) request.getSession().getAttribute( PentahoSystem.PENTAHO_SESSION_KEY );
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
  // endregion
}
