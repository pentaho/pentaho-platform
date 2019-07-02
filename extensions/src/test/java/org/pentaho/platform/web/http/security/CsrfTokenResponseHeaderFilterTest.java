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
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.security;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

public class CsrfTokenResponseHeaderFilterTest {

  private HttpServletRequest mockRequest;
  private HttpServletResponse mockResponse;
  private CsrfTokenResponseHeaderFilter filter;
  private FilterChain filterChain;

  @Before
  public void setUp() throws Exception {

    this.mockRequest = mock( HttpServletRequest.class );
    this.mockResponse = mock( HttpServletResponse.class );
    this.filter = new CsrfTokenResponseHeaderFilter();
    this.filterChain = new MockFilterChain();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testWorks() throws Exception {

    MockFilterConfig cfg = new MockFilterConfig();

    filter.init( cfg );

    filter.doFilter( this.mockRequest, this.mockResponse, this.filterChain );

    Mockito.verify( mockResponse, Mockito.never() ).setHeader( Mockito.anyString(), Mockito.anyString() );
  }
}
