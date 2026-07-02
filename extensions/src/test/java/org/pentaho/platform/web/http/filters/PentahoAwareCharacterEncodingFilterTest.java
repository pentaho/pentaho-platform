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


package org.pentaho.platform.web.http.filters;

import org.springframework.mock.web.MockFilterChain;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

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
    filter.doFilter((ServletRequest) request, (ServletResponse) new MockHttpServletResponse(), (FilterChain) new MockFilterChain());

    assertEquals( NEW_ENCODING, request.getCharacterEncoding() );
  }

  @Test
  public void doFilter_ignoreFlagIsCleared_requestEncodingIsNull() throws Exception {
    filterConfig.addInitParameter( "ignore", Boolean.FALSE.toString() );

    request.setCharacterEncoding( null );

    filter.init( filterConfig );
    filter.doFilter((ServletRequest) request, (ServletResponse) new MockHttpServletResponse(), (FilterChain) new MockFilterChain());

    assertEquals( NEW_ENCODING, request.getCharacterEncoding() );
  }

  @Test
  public void doFilter_ignoreFlagIsCleared_requestEncodingNotNull() throws Exception {
    filterConfig.addInitParameter( "ignore", Boolean.FALSE.toString() );

    request.setCharacterEncoding( NEW_ENCODING );

    filter.init( filterConfig );
    filter.doFilter((ServletRequest) request, (ServletResponse) new MockHttpServletResponse(), (FilterChain) new MockFilterChain());

    assertEquals( NEW_ENCODING, request.getCharacterEncoding() );
  }

  @Test
  public void doFilter_noEncodingIsProvided() throws Exception {
    filterConfig.addInitParameter( "ignore", Boolean.FALSE.toString() );

    request.setCharacterEncoding( null );

    MockServletContext ctx = (MockServletContext) request.getSession().getServletContext();
    ctx.addInitParameter( PentahoAwareCharacterEncodingFilter.INIT_PARAM_ENCODING, "" );

    filter.init( filterConfig );
    filter.doFilter((ServletRequest) request, (ServletResponse) new MockHttpServletResponse(), (FilterChain) new MockFilterChain());

    assertEquals( PentahoAwareCharacterEncodingFilter.DEFAULT_CHAR_ENCODING, request.getCharacterEncoding() );
  }
}