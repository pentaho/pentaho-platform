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

/**
 * This filter is designed to work around a limitation of some web servers which prohibit the encoded form of / and \
 * (%2F and %5C) being in the URL. Clients can pass double-encoded forms of these encodings instead (%252F and %255C).
 * This filter will detect any occurance of these double encodings and correct them before passing on to the rest of the
 * servlet chain.
 * <p/>
 * Created by nbaker on 4/8/14.
 */
public class PentahoPathDecodingFilter implements Filter {
  @Override
  public void init( FilterConfig filterConfig ) throws ServletException {

  }

  @Override
  public void doFilter( ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain )
    throws IOException, ServletException {
    EncodingAwareHttpServletRequestWrapper wrapper = new EncodingAwareHttpServletRequestWrapper(
        (HttpServletRequest) servletRequest );
    filterChain.doFilter( wrapper, servletResponse );
  }

  @Override
  public void destroy() {

  }

  private static final class EncodingAwareHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private EncodingAwareHttpServletRequestWrapper( HttpServletRequest request ) {
      super( request );
    }

    @Override
    public String getPathInfo() {
      final String pathInfo = super.getPathInfo();
      if ( StringUtils.isEmpty( pathInfo ) ) {
        return pathInfo;
      }
      return pathInfo.replaceAll( "%5C", "\\\\" ).replaceAll( "%2F", "/" );
    }

    @Override
    public String getRequestURI() {
      final String requestURI = super.getRequestURI();
      return requestURI.replaceAll( "%255C", "%5C" ).replaceAll( "%252F", "%2F" );
    }
  }
}
