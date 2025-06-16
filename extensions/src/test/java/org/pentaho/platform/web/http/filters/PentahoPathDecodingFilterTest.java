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

import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;

/**
 * Created by nbaker on 4/8/14.
 */
public class PentahoPathDecodingFilterTest {
  @Test
  public void testDoFilter() throws Exception {
    PentahoPathDecodingFilter decodingFilter = new PentahoPathDecodingFilter();
    final MockHttpServletRequest request =
        new MockHttpServletRequest( "GET", "http://localhost:8080/pentaho/encoded%255Cpath" );
    request.setPathInfo( "/pentaho/encoded%5Cpath" );
    decodingFilter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() {
      @Override
      public void doFilter( ServletRequest request, ServletResponse response ) {
        //pathInfo is to be fully decoded
        assertEquals( "/pentaho/encoded\\path", ( (HttpServletRequest) request ).getPathInfo() );
        //requestURI is not decoded
        assertEquals( "http://localhost:8080/pentaho/encoded%5Cpath", ( (HttpServletRequest) request ).getRequestURI() );
      }
    } );


  }
}
