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

import com.mockrunner.mock.web.MockFilterChain;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class PentahoAwareCharacterEncodingFilterTest {

  private static final String NEW_ENCODING = "win-1252";

  private PentahoAwareCharacterEncodingFilter filter;
  private HttpServletRequest request;
  private MockFilterConfig filterConfig;

  @Before
  public void setUp() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    HttpSession session = req.getSession();
    MockServletContext ctx = (MockServletContext) session.getServletContext();
    ctx.addInitParameter( PentahoAwareCharacterEncodingFilter.INIT_PARAM_ENCODING, NEW_ENCODING );

    request = spy( req );
    doReturn( session ).when( request ).getSession( anyBoolean() );

    filterConfig = new MockFilterConfig();

    filter = new PentahoAwareCharacterEncodingFilter();
  }


  @Test
  public void doFilter_ignoreFlagIsSet() throws Exception {
    filterConfig.addInitParameter( "ignore", Boolean.TRUE.toString() );

    filter.init( filterConfig );
    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    assertEquals( NEW_ENCODING, request.getCharacterEncoding() );
  }

  @Test
  public void doFilter_ignoreFlagIsCleared_requestEncodingIsNull() throws Exception {
    filterConfig.addInitParameter( "ignore", Boolean.FALSE.toString() );

    request.setCharacterEncoding( null );

    filter.init( filterConfig );
    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    assertEquals( NEW_ENCODING, request.getCharacterEncoding() );
  }

  @Test
  public void doFilter_ignoreFlagIsCleared_requestEncodingNotNull() throws Exception {
    filterConfig.addInitParameter( "ignore", Boolean.FALSE.toString() );

    request.setCharacterEncoding( NEW_ENCODING );

    filter.init( filterConfig );
    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    assertEquals( NEW_ENCODING, request.getCharacterEncoding() );
  }

  @Test
  public void doFilter_noEncodingIsProvided() throws Exception {
    filterConfig.addInitParameter( "ignore", Boolean.FALSE.toString() );

    request.setCharacterEncoding( null );

    MockServletContext ctx = (MockServletContext) request.getSession().getServletContext();
    ctx.addInitParameter( PentahoAwareCharacterEncodingFilter.INIT_PARAM_ENCODING, "" );

    filter.init( filterConfig );
    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    assertEquals( PentahoAwareCharacterEncodingFilter.DEFAULT_CHAR_ENCODING, request.getCharacterEncoding() );
  }
}