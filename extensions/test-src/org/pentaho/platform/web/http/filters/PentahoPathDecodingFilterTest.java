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

import static org.junit.Assert.assertEquals;

import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Created by nbaker on 4/8/14.
 */
public class PentahoPathDecodingFilterTest {

  private static final String russianDecodedValue = "%D0%B7%D0%BD%D0%B0%D1%87%D0%B5%D0%BD%D0%B8%D0%B5";
  private static String russianValue;

  static {
    try {
      russianValue = URIUtil.decode( russianDecodedValue );
    } catch ( URIException e ) {
      e.printStackTrace();
    }
  }

  @Test
  public void testDoFilter() throws Exception {
    PentahoPathDecodingFilter decodingFilter = new PentahoPathDecodingFilter();
    MockHttpServletRequest request =
        new MockHttpServletRequest( "GET", "http://localhost:8080/pentaho/encoded%255Cpath" );
    request.setQueryString( "a=%D0%B7%D0%BD%D0%B0%D1%87%D0%B5%D0%BD%D0%B8%D0%B5&a=value" );
    request.setPathInfo( "/pentaho/encoded%5Cpath" );
    request.setCharacterEncoding( "UTF-8" );
    decodingFilter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() {
      @Override
      public void doFilter( ServletRequest request, ServletResponse response ) {
        HttpServletRequest rq = ( (HttpServletRequest) request );
        //pathInfo is to be fully decoded
        assertEquals( "/pentaho/encoded\\path", rq.getPathInfo() );
        //requestURI is not decoded
        assertEquals( "http://localhost:8080/pentaho/encoded%5Cpath", rq.getRequestURI() );

        String[] parameters = request.getParameterValues( "a" );
        assertEquals( 2, parameters.length );
        assertEquals( russianValue, parameters[0] );
        assertEquals( "value", parameters[1] );

        assertEquals( russianValue, request.getParameter( "a" ) );

        Map<String, String[]> map = request.getParameterMap();
        assertEquals( russianValue, map.get( "a" )[0] );

        assertEquals( "a", request.getParameterNames().nextElement() );
      }
    } );

    request = new MockHttpServletRequest( "POST", "http://localhost:8080/pentaho?v=x" );
    request.setQueryString( "v=x" );
    request.setParameter( "a", new String[] { "b", "c" } );
    decodingFilter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() {
      @Override
      public void doFilter( ServletRequest request, ServletResponse response ) {
        String[] parameters = request.getParameterValues( "a" );
        assertEquals( 2, parameters.length );
        assertEquals( "b", parameters[0] );
        assertEquals( "c", parameters[1] );

        assertEquals( "b", request.getParameter( "a" ) );

        Map<String, String[]> map = request.getParameterMap();
        assertEquals( "b", map.get( "a" )[0] );

        assertEquals( "a", request.getParameterNames().nextElement() );
      }
    } );
  }

}
