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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.security.Authentication;
import org.springframework.security.context.HttpSessionContextIntegrationFilter;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 
 * This servlet is used to filter Servlet requests coming from another server for processing and sets authentication for
 * the user passed in by the parameter <b>_TRUST_USER_</b>. It will conditionally look for the parameter in the HTTP
 * Header too. It then passes the request down the servlet chain to be serviced. Only requests coming from a trusted
 * host will be authenticated. Implement the filter and setup the trusted hosts by editing the <b>web.xml</b> file as
 * follows.
 * <p>
 * 
 * <pre>
 * 
 *  &lt;filter&gt;
 *    &lt;filter-name&gt;ProxyTrustingFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;org.pentaho.platform.web.http.filters.ProxyTrustingFilter&lt;/filter-class&gt;
 *      &lt;init-param&gt;
 *        &lt;param-name&gt;TrustedIpAddrs&lt;/param-name&gt;
 *        &lt;param-value&gt;192.168.10.60,192.168.10.61&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *  &lt;/filter&gt;
 * </pre>
 * 
 * In the above example, when a request coming from IP addresses 192.168.10.60 and 192.168.10.61 has the parameter
 * _TRUST_USER_=<i>name</i> set, tha user <i>name</i> will be authenticated.
 * 
 * <p>
 * NOTES:
 * <p>
 * 
 * It is easy to spoof the URL or IP address so this technique should only be used if the server running the filter is
 * not accessible to users. For example if the BI Platform is hosted in a DMZ.
 * <p>
 * 
 * For this class to be useful, both Pentaho servers should be using the same database repository.
 * <p>
 * The sending server should be using the ProxyServlet enabled to generate the requests.
 * <p>
 * 
 * The parameter that this filter looks for can be configured in the init parameters, as well as whether to check the
 * request headers. The following shows the defaults used if these settings aren't provided.
 * 
 * <pre>
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;CheckHeader&lt;/param-name&gt;
 *     &lt;param-value&gt;true&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;RequestParameterName&lt;/param-name&gt;
 *     &lt;param-value&gt;_TRUST_USER_&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;HeaderName&lt;/param-name&gt;
 *     &lt;param-value&gt;_TRUST_USER_&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * </pre>
 * 
 * 
 * @see org.pentaho.platform.web.servlet.ProxyServlet
 * @author Doug Moran
 * 
 */

public class ProxyTrustingFilter implements Filter {
  FilterConfig filterConfig;
  private static final String DefaultParameterName = "_TRUST_USER_"; //$NON-NLS-1$

  String[] trustedIpAddrs = null;
  private boolean checkHeader = true;
  private String requestParameterName;
  private String headerName;
  private Map<String, Pattern> ipPatterns = new HashMap<String, Pattern>();

  private static final Log logger = LogFactory.getLog( ProxyTrustingFilter.class );

  public Log getLogger() {
    return ProxyTrustingFilter.logger;
  }

  public void init( final FilterConfig filterConfiguration ) throws ServletException {
    this.filterConfig = filterConfiguration;

    trustedIpAddrs = null;
    String hostStr = filterConfig.getInitParameter( "TrustedIpAddrs" ); //$NON-NLS-1$
    if ( hostStr != null ) {
      StringTokenizer st = new StringTokenizer( hostStr, "," ); //$NON-NLS-1$
      List<String> addrs = new ArrayList<String>();
      while ( st.hasMoreTokens() ) {
        String tok = st.nextToken().trim();
        if ( tok.length() > 0 ) {
          addrs.add( tok );
          // getLogger().info(
          // Messages.getString("ProxyTrustingFilter.DEBUG_0001_TRUSTING",
          // tok ) ); //$NON-NLS-1$
        }
      }
      if ( addrs.size() > 0 ) { // Guarantee that its null or has at least 1
        // element
        trustedIpAddrs = (String[]) addrs.toArray( new String[0] );
      }
    }
    String checkHeaderString = filterConfig.getInitParameter( "CheckHeader" ); //$NON-NLS-1$
    if ( !isEmpty( checkHeaderString ) ) {
      this.checkHeader = checkHeaderString.equalsIgnoreCase( "true" ); //$NON-NLS-1$
    }
    String requestParameterSetting = filterConfig.getInitParameter( "RequestParameterName" ); //$NON-NLS-1$
    if ( !isEmpty( requestParameterSetting ) ) {
      this.requestParameterName = requestParameterSetting;
    } else {
      this.requestParameterName = ProxyTrustingFilter.DefaultParameterName;
    }
    String headerNameSetting = filterConfig.getInitParameter( "HeaderName" ); //$NON-NLS-1$
    if ( !isEmpty( headerNameSetting ) ) {
      this.headerName = headerNameSetting;
    } else {
      this.headerName = ProxyTrustingFilter.DefaultParameterName;
    }
  }

  boolean isTrusted( final String addr ) {
    if ( trustedIpAddrs != null ) {
      for ( String element : trustedIpAddrs ) {
        if ( element.equals( addr ) ) {
          return ( true );
        }
        // Reuse an pattern for this element
        Pattern pat = ipPatterns.get( element );
        if ( pat == null ) {
          // first one created, put it in the map for re-use
          try {
            pat = Pattern.compile( element );
            ipPatterns.put( element, pat );
          } catch ( PatternSyntaxException ignored ) {
            continue;
          }
        }
        Matcher matcher = pat.matcher( addr );
        if ( matcher.find() ) {
          return ( true );
        }
      }
    }
    return ( false );
  }

  public void doFilter( final ServletRequest request, final ServletResponse response, final FilterChain chain )
    throws IOException, ServletException {

    // long startTime = System.currentTimeMillis();

    if ( ( trustedIpAddrs != null ) && ( request instanceof HttpServletRequest ) ) {
      final HttpServletRequest req = (HttpServletRequest) request;
      String remoteHost = req.getRemoteAddr();

      if ( isTrusted( remoteHost ) ) {
        String name = getTrustUser( req );
        if ( !isEmpty( name ) ) {

          try {
            SecurityHelper.getInstance().runAsUser( name, new Callable<Void>() {

              @Override
              public Void call() throws Exception {
                HttpSession httpSession = req.getSession();

                httpSession.setAttribute( PentahoSystem.PENTAHO_SESSION_KEY, PentahoSessionHolder.getSession() );

                /**
                 * definition of anonymous inner class
                 */
                SecurityContext authWrapper = new SecurityContext() {
                  /**
                     * 
                     */
                  private static final long serialVersionUID = 1L;
                  private Authentication authentication;

                  public Authentication getAuthentication() {
                    return authentication;
                  };

                  public void setAuthentication( Authentication authentication ) {
                    this.authentication = authentication;
                  };
                }; // end anonymous inner class

                authWrapper.setAuthentication( SecurityContextHolder.getContext().getAuthentication() );
                httpSession.setAttribute( HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY,
                  authWrapper );
                return null;
              }

            } );
          } catch ( Exception e ) {
            throw new ServletException( e );
          }

        }
      }
    }
    chain.doFilter( request, response );

    // long stopTime = System.currentTimeMillis();

    // getLogger().debug( Messages.getString(
    // Messages.getString("ProxyTrustingFilter.DEBUG_0004_REQUEST_TIME"),
    // String.valueOf( stopTime - startTime ) ) ); //$NON-NLS-1$
  }

  public void destroy() {

  }

  /**
   * @param args
   */
  public static void main( final String[] args ) {

  }

  /**
   * 
   * @return the name of the request header that will contain the trusted user name
   */
  protected String getHeaderName() {
    return this.headerName;
  }

  /**
   * @return the name of the request parameter that will contain the trusted user name
   */
  protected String getParameterName() {
    return this.requestParameterName;
  }

  /**
   * @return true if the filter should consult the http header for the trusted user
   */
  protected boolean checkHeader() {
    return checkHeader;
  }

  /**
   * Gets the trusted user from the request, and optionally from the HTTP Header
   * 
   * @param request
   *          The HttpServletRequest to examine for the trusted information
   * @return The name of the trusted user
   */
  protected String getTrustUser( HttpServletRequest request ) {
    String name = request.getParameter( getParameterName() );
    if ( checkHeader() && isEmpty( name ) ) {
      name = request.getHeader( normalizeHeaderName( getHeaderName() ) );
    }

    return name;
  }

  public boolean isEmpty( String str ) {
    return ( ( str == null ) || ( str.length() == 0 ) );
  }

  protected String normalizeHeaderName( final String in ) {
    String lower = in.toLowerCase();
    return Character.toUpperCase( lower.charAt( 0 ) ) + lower.substring( 1 );
  }

}
