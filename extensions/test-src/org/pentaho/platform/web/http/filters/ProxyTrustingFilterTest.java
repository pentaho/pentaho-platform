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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.filters;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

    MockFilterConfig cfg = new MockFilterConfig();
    cfg.addInitParameter( "TrustedIpAddrs", "1.1.1.1," + TRUSTED_IP );

    filter = new ProxyTrustingFilter();
    filter.init( cfg );
  }

  @After
  public void tearDown() {
    SecurityHelper.setMockInstance( null );
  }


  @Test
  public void doFilterForTrusted() throws Exception {
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
    request.setRemoteHost( UNTRUSTED_IP );

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    verify( securityHelper, never() ).runAsUser( anyString(), any( Callable.class ) );
  }

}
