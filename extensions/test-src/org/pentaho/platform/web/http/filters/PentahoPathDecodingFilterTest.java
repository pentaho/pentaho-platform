package org.pentaho.platform.web.http.filters;

import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;

/**
 * Created by nbaker on 4/8/14.
 */
public class PentahoPathDecodingFilterTest {
  @Test
  public void testDoFilter() throws Exception {
    PentahoPathDecodingFilter decodingFilter = new PentahoPathDecodingFilter();

    // Check PathInfo and RequestURI
    MockHttpServletRequest request =
      new MockHttpServletRequest( "GET", "http://localhost:8080/pentaho/encoded%255Cpath" );
    request.setPathInfo( "/pentaho/encoded%5Cpath" );
    decodingFilter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() {
      @Override public void doFilter( ServletRequest request, ServletResponse response ) {
        //pathInfo is to be fully decoded
        assertEquals( "/pentaho/encoded\\path", ( (HttpServletRequest) request ).getPathInfo() );
        //requestURI is not decoded
        assertEquals( "http://localhost:8080/pentaho/encoded%5Cpath",
          ( (HttpServletRequest) request ).getRequestURI() );
      }
    } );

    // Test query-string and parameter values with GET request
    request =
      new MockHttpServletRequest( "GET",
        "http://localhost:8080/pentaho/encoded%255Cpath?val1=%255Chome&val2=%252Fpublic" );
    request.setParameter( "val1", "%5Chome" );
    request.setParameter( "val2", "%2Fhome" );
    request.setQueryString( "val1=%255Chome&val2=%252Fpublic" );
    decodingFilter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() {
      @Override public void doFilter( ServletRequest request, ServletResponse response ) {

        assertEquals( "\\home", request.getParameter( "val1" ) );
        assertEquals( "/home", request.getParameter( "val2" ) );

        assertEquals( "val1=%5Chome&val2=%2Fpublic", ( (HttpServletRequest) request ).getQueryString() );
      }
    } );

    // Test query-string and parameter values with POST request
    request =
      new MockHttpServletRequest( "POST",
        "http://localhost:8080/pentaho/something" );
    request.setParameter( "val1", "\\home" );
    request.setParameter( "val2", "/home" );

    decodingFilter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() {
      @Override public void doFilter( ServletRequest request, ServletResponse response ) {

        assertEquals( "\\home", request.getParameter( "val1" ) );
        assertEquals( "/home", request.getParameter( "val2" ) );

      }
    } );


  }
}
