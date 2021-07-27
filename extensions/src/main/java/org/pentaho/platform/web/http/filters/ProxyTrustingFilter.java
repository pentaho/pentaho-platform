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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.UserSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

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
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
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
 * <p>
 * In the above example, when a request coming from IP addresses 192.168.10.60 and 192.168.10.61 has the parameter
 * _TRUST_USER_=<i>name</i> set, that user <i>name</i> will be authenticated.
 * <p>
 * An additional parameter, <b>_TRUST_LOCALE_OVERRIDE_</b>, and, optionally, header, can be specified containing a
 * locale override that the user session should use.
 *
 * <p>
 * NOTES:
 * <p>
 * <p>
 * It is easy to spoof the URL or IP address so this technique should only be used if the server running the filter is
 * not accessible to users. For example if the BI Platform is hosted in a DMZ.
 * <p>
 * <p>
 * For this class to be useful, both Pentaho servers should be using the same database repository.
 * <p>
 * The sending server should be using the ProxyServlet enabled to generate the requests.
 * <p>
 * <p>
 * The user and locale parameters that this filter looks for can be configured in the init parameters, as well as
 * whether to check the request headers.
 * <p>
 * The following shows the defaults used if these settings aren't provided.
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
 *   &lt;init-param&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;LocaleOverrideParameterName&lt;/param-name&gt;
 *     &lt;param-value&gt;_TRUST_LOCALE_OVERRIDE_&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;LocaleOverrideHeaderName&lt;/param-name&gt;
 *     &lt;param-value&gt;_TRUST_LOCALE_OVERRIDE_&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * </pre>
 *
 * @author Doug Moran
 * @see org.pentaho.platform.web.servlet.ProxyServlet
 */

public class ProxyTrustingFilter implements Filter {
  FilterConfig filterConfig;
  private static final String DEFAULT_PARAMETER_NAME = "_TRUST_USER_";
  private static final String DEFAULT_LOCALE_OVERRIDE_PARAMETER_NAME = "_TRUST_LOCALE_OVERRIDE_";

  String[] trustedIpAddrs = null;
  private boolean checkHeader = true;

  private String requestParameterName;
  private String headerName;

  private String localeOverrideParameterName;
  private String localeOverrideHeaderName;

  private final Map<String, Pattern> ipPatterns = new HashMap<>();

  private static final Log logger = LogFactory.getLog( ProxyTrustingFilter.class );

  public Log getLogger() {
    return ProxyTrustingFilter.logger;
  }

  // region init( config )
  public void init( final FilterConfig filterConfiguration ) throws ServletException {
    this.filterConfig = filterConfiguration;

    initParameterTrustedIpAddresses();

    initParameterCheckHeader();

    initParameterUser();

    initParameterLocaleOverride();
  }

  protected void initParameterTrustedIpAddresses() {
    trustedIpAddrs = null;
    String hostStr = filterConfig.getInitParameter( "TrustedIpAddrs" );
    if ( hostStr != null ) {
      StringTokenizer st = new StringTokenizer( hostStr, "," );
      List<String> addrs = new ArrayList<String>();
      while ( st.hasMoreTokens() ) {
        String tok = st.nextToken().trim();
        if ( tok.length() > 0 ) {
          addrs.add( tok );
        }
      }
      if ( addrs.size() > 0 ) { // Guarantee that its null or has at least 1
        // element
        trustedIpAddrs = addrs.toArray( new String[ 0 ] );
      }
    }
  }

  protected void initParameterCheckHeader() {
    String checkHeaderString = filterConfig.getInitParameter( "CheckHeader" );
    if ( !isEmpty( checkHeaderString ) ) {
      this.checkHeader = checkHeaderString.equalsIgnoreCase( "true" );
    }
  }

  protected void initParameterUser() {
    String requestParameterSetting = filterConfig.getInitParameter( "RequestParameterName" );
    if ( !isEmpty( requestParameterSetting ) ) {
      this.requestParameterName = requestParameterSetting;
    } else {
      this.requestParameterName = ProxyTrustingFilter.DEFAULT_PARAMETER_NAME;
    }

    String headerNameSetting = filterConfig.getInitParameter( "HeaderName" );
    if ( !isEmpty( headerNameSetting ) ) {
      this.headerName = headerNameSetting;
    } else {
      this.headerName = ProxyTrustingFilter.DEFAULT_PARAMETER_NAME;
    }
  }

  protected void initParameterLocaleOverride() {
    String localeOverrideParameterSetting = filterConfig.getInitParameter( "LocaleOverrideParameterName" );
    if ( !isEmpty( localeOverrideParameterSetting ) ) {
      this.localeOverrideParameterName = localeOverrideParameterSetting;
    } else {
      this.localeOverrideParameterName = DEFAULT_LOCALE_OVERRIDE_PARAMETER_NAME;
    }
    String localeOverrideHeaderNameSetting = filterConfig.getInitParameter( "LocaleOverrideHeaderName" );
    if ( !isEmpty( localeOverrideHeaderNameSetting ) ) {
      this.localeOverrideHeaderName = localeOverrideHeaderNameSetting;
    } else {
      this.localeOverrideHeaderName = DEFAULT_LOCALE_OVERRIDE_PARAMETER_NAME;
    }
  }
  // endregion

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

    if ( ( trustedIpAddrs != null ) && ( request instanceof HttpServletRequest ) ) {
      final HttpServletRequest req = (HttpServletRequest) request;
      String remoteHost = req.getRemoteAddr();

      if ( isTrusted( remoteHost ) ) {
        String name = getTrustUser( req );
        if ( !isEmpty( name ) ) {
          doFilterCore( req, name );
        }
      }
    }

    chain.doFilter( request, response );
  }

  protected void doFilterCore( HttpServletRequest request, String name ) throws ServletException {
    try {
      // Create a Pentaho user session and make it current.
      becomeUser( name );

      HttpSession httpSession = request.getSession();

      // Associate the new Pentaho session with the HTTP session.
      //
      // Despite that we're already setting the session in PentahoSessionHolder,
      // because HttpSessionPentahoSessionIntegrationFilter runs afterwards, as configured by default,
      // and calls PentahoSessionHolder.setSession( session ) with the session in this attribute,
      // we need to set the attribute any way.
      httpSession.setAttribute( PentahoSystem.PENTAHO_SESSION_KEY, PentahoSessionHolder.getSession() );

      // Locale Override
      //
      // Despite that we're setting the thread locale override,
      // because HttpSessionPentahoSessionIntegrationFilter runs afterwards, as configured by default,
      // and calls LocaleHelper.setThreadLocaleOverride( locale ) with the locale in this attribute,
      // we need to set the attribute any way.
      String localeOverrideCode = getTrustLocaleOverrideCode( request );
      httpSession.setAttribute( IPentahoSession.ATTRIBUTE_LOCALE_OVERRIDE, localeOverrideCode );

      setSystemLocaleOverrideCode( localeOverrideCode );

      // Create and associate a Spring Security Context with the HTTP session.
      SecurityContext authWrapper = createSpringSecurityContext();
      httpSession.setAttribute( HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, authWrapper );
    } catch ( Exception e ) {
      throw new ServletException( e );
    }
  }

  private SecurityContext createSpringSecurityContext() {
    SecurityContext authWrapper = new SecurityContext() {
      private static final long serialVersionUID = 1L;
      private Authentication authentication;

      public Authentication getAuthentication() {
        return authentication;
      }

      public void setAuthentication( Authentication authentication ) {
        this.authentication = authentication;
      }
    };

    authWrapper.setAuthentication( SecurityContextHolder.getContext().getAuthentication() );

    return authWrapper;
  }

  public void destroy() {

  }

  /**
   * @param args
   */
  public static void main( final String[] args ) {

  }

  /**
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
   * Gets the name of the request header that will contain the trusted locale override
   */
  protected String getLocaleOverrideHeaderName() {
    return this.localeOverrideHeaderName;
  }

  /**
   * Gets the name of the request parameter that will contain the trusted locale override.
   */
  protected String getLocaleOverrideParameterName() {
    return this.localeOverrideParameterName;
  }

  /**
   * @return true if the filter should consult the http header for the trusted user and trusted locale override
   */
  protected boolean checkHeader() {
    return checkHeader;
  }

  /**
   * Gets the trusted user from the request, and optionally from the HTTP Header
   *
   * @param request The HttpServletRequest to examine for the trusted information
   * @return The name of the trusted user
   */
  protected String getTrustUser( HttpServletRequest request ) {
    String name = request.getParameter( getParameterName() );
    if ( checkHeader() && isEmpty( name ) ) {
      name = request.getHeader( normalizeHeaderName( getHeaderName() ) );
    }

    return name;
  }

  /**
   * Gets the trusted locale code override from the request, and optionally from the HTTP header.
   *
   * @param request The HttpServletRequest to examine for the trusted information
   * @return The specified trusted locale code, if any; {@code null}, otherwise.
   */
  protected String getTrustLocaleOverrideCode( HttpServletRequest request ) {
    String localeOverrideCode = request.getParameter( getLocaleOverrideParameterName() );
    if ( checkHeader() && isEmpty( localeOverrideCode ) ) {
      localeOverrideCode = request.getHeader( normalizeHeaderName( getLocaleOverrideHeaderName() ) );
    }

    return localeOverrideCode;
  }

  public boolean isEmpty( String str ) {
    return ( ( str == null ) || ( str.length() == 0 ) );
  }

  protected String normalizeHeaderName( final String in ) {
    String lower = in.toLowerCase();
    return Character.toUpperCase( lower.charAt( 0 ) ) + lower.substring( 1 );
  }

  // cloned from SecurityHelper and adapted to add a default location to the session
  protected void becomeUser( final String principalName ) {
    UserSession session;
    Locale locale = Locale.getDefault();
    ITenantedPrincipleNameResolver tenantedUserNameUtils =
      PentahoSystem.get( ITenantedPrincipleNameResolver.class, "tenantedUserNameUtils", null );
    if ( tenantedUserNameUtils != null ) {
      session = new UserSession( principalName, locale, false, null );
      ITenant tenant = tenantedUserNameUtils.getTenant( principalName );
      session.setAttribute( IPentahoSession.TENANT_ID_KEY, tenant.getId() );
      session.setAuthenticated( tenant.getId(), principalName );
    } else {
      session = new UserSession( principalName, locale, false, null );
      session.setAuthenticated( principalName );
    }

    PentahoSessionHolder.setSession( session );

    Authentication auth = SecurityHelper.getInstance().createAuthentication( principalName );
    SecurityContextHolder.getContext().setAuthentication( auth );

    PentahoSystem.sessionStartup( PentahoSessionHolder.getSession(), null );
  }

  /**
   * Sets the system's locale override.
   *
   * @param localeOverrideCode The locale override code.
   */
  protected void setSystemLocaleOverrideCode( String localeOverrideCode ) {
    LocaleHelper.setThreadLocaleOverride( LocaleHelper.parseLocale( localeOverrideCode ) );
  }
}
