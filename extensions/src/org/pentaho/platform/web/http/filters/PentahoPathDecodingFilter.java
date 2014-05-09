package org.pentaho.platform.web.http.filters;

import org.drools.util.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * This filter is designed to work around a limitation of some web servers which prohibit the encoded form of / and \
 * (%2F and %5C) being in the URL. Clients can pass double-encoded forms of these encodings instead (%252F and %255C).
 * This filter will detect any occurance of these double encodings and correct them before passing on to the rest of the
 * servlet chain.
 * <p/>
 * Created by nbaker on 4/8/14.
 */
public class PentahoPathDecodingFilter implements Filter {
  @Override public void init( FilterConfig filterConfig ) throws ServletException {

  }

  @Override
  public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain )
    throws IOException, ServletException {
    EncodingAwareHttpServletRequestWrapper wrapper = new EncodingAwareHttpServletRequestWrapper(
      (HttpServletRequest) servletRequest );
    filterChain.doFilter( wrapper, servletResponse );
  }

  @Override public void destroy() {

  }

  private static final class EncodingAwareHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private EncodingAwareHttpServletRequestWrapper( HttpServletRequest request ) {
      super( request );
    }

    @Override public String getPathInfo() {
      final String pathInfo = super.getPathInfo();
      if ( StringUtils.isEmpty( pathInfo ) ) {
        return pathInfo;
      }
      return decodeSingleEncoded( pathInfo );
    }

    @Override public String getRequestURI() {
      final String requestURI = super.getRequestURI();
      return decodeDoubleEncoding( requestURI );
    }

    /**
     * Not decoded by the superclass
     *
     * @return
     */
    @Override public String getQueryString() {
      final String queryString = super.getQueryString();
      if ( !StringUtils.isEmpty( queryString ) ) {
        return decodeDoubleEncoding( queryString );
      }
      return null;
    }

    /**
     * Decoded by the superclass
     *
     * @param name
     * @return
     */
    @Override public String getParameter( String name ) {
      if ( this.getMethod().equals( "POST" ) ) {
        // POST payload parameters need no special encoding. A decode would be destructive. Unfortunately we cannot
        // easily know if a parameter came from the query-string or the post data. So we'll return the original value
        // for now.
        // TODO: handle mix of POST data and query-string parameters
        return super.getParameter( name );
      }
      final String parameter = super.getParameter( name );
      if ( !StringUtils.isEmpty( parameter ) ) {
        return decodeSingleEncoded( parameter );
      }
      return null;
    }

    @Override public String[] getParameterValues( String name ) {
      if ( this.getMethod().equals( "POST" ) ) {
        // POST payload parameters need no special encoding. A decode would be destructive. Unfortunately we cannot
        // easily know if a parameter came from the query-string or the post data. So we'll return the original value
        // for now.
        // TODO: handle mix of POST data and query-string parameters
        return super.getParameterValues( name );
      }
      final String[] parameterValues = super.getParameterValues( name );
      for ( int i = 0; i < parameterValues.length; i++ ) {
        String parameterValue = parameterValues[ i ];
        parameterValues[ i ] = decodeSingleEncoded( parameterValue );
      }
      return parameterValues;
    }

    @Override public Enumeration getParameterNames() {
      if ( this.getMethod().equals( "POST" ) ) {
        // POST payload parameters need no special encoding. A decode would be destructive. Unfortunately we cannot
        // easily know if a parameter came from the query-string or the post data. So we'll return the original value
        // for now.
        // TODO: handle mix of POST data and query-string parameters
        return super.getParameterNames();
      }
      final Enumeration parameterNames = super.getParameterNames();
      List<String> decoded = new ArrayList<String>();
      while ( parameterNames.hasMoreElements() ) {
        String o = parameterNames.nextElement().toString();
        decoded.add( decodeSingleEncoded( o ) );
      }
      return Collections.enumeration( decoded );
    }

    /**
     * Decodes Doubly-Encoded forward and backslashes
     *
     * @param input
     * @return
     */
    private static String decodeDoubleEncoding( String input ) {
      return input.replaceAll( "%255C", "%5C" ).replaceAll( "%252F", "%2F" );
    }


    /**
     * Decodes Single-Encoded forward and backslashes
     *
     * @param input
     * @return
     */
    private static String decodeSingleEncoded( String input ) {
      return input.replaceAll( "%5C", "\\\\" ).replaceAll( "%2F", "/" );
    }

  }
}
