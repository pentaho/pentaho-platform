package org.pentaho.platform.web.http.filters;

import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import java.util.Collections;
import java.util.Enumeration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    request.setParameter( "val%5C", "something" );
    request.setQueryString( "val1=%255Chome&val2=%252Fpublic" );
    decodingFilter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() {
      @Override public void doFilter( ServletRequest request, ServletResponse response ) {

        assertEquals( "\\home", request.getParameter( "val1" ) );
        assertEquals( "/home", request.getParameter( "val2" ) );

        assertEquals( "val1=%5Chome&val2=%2Fpublic", ( (HttpServletRequest) request ).getQueryString() );
        final Enumeration parameterNames = request.getParameterNames();
        boolean foundDecodedParam  = false;
        while ( parameterNames.hasMoreElements() ) {
          String o =  parameterNames.nextElement().toString();
          if(o.equals( "val\\" ) ){
            foundDecodedParam = true;
            break;
          }
        }
        assertTrue( foundDecodedParam  );
      }
    } );

    // Test query-string and parameter values with POST request
    request =
      new MockHttpServletRequest( "POST",
        "http://localhost:8080/pentaho/something" );
    request.setParameter( "val1", "\\home" );
    request.setParameter( "val2", "/home" );
    request.setParameter( "val%255C", "something" );

    decodingFilter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() {
      @Override public void doFilter( ServletRequest request, ServletResponse response ) {

        assertEquals( "\\home", request.getParameter( "val1" ) );
        assertEquals( "/home", request.getParameter( "val2" ) );
        assertEquals( "something", request.getParameter( "val%255C" ) );

      }
    } );


  }
}
