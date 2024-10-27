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


package org.pentaho.platform.web.http.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.web.http.messages.Messages;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Detects when an HTTP session which contains a logged-in user (as indicated by <code>request.getRemoteUser()</code>)
 * is attempting to authenticate again without logging out. Upon detecting this condition, the session is invalidated,
 * the security context is cleared, and the user is redirected to <code>sessionReuseDetectedUrl</code>. This prevents
 * reuse of an HTTP session which contains potentially sensitive, user-specific data.
 * 
 * <p>
 * To use: Insert after <code>httpSessionContextIntegrationFilter</code> but before
 * <code>authenticationProcessingFilter</code>.
 * </p>
 * 
 * <p>
 * Note: Some code copied from <code>AbstractProcessingFilter</code>.
 * </p>
 * 
 * @author mlowery
 */
public class HttpSessionReuseDetectionFilter implements Filter, InitializingBean {

  // ~ Static fields/initializers ============================================

  private static final Log logger = LogFactory.getLog( HttpSessionReuseDetectionFilter.class );

  // ~ Instance fields =======================================================

  /**
   * This should be the same URL that is set for
   * <code>org.springframework.security.ui.webapp.AuthenticationProcessingFilter</code>.
   */
  private String filterProcessesUrl;

  private String sessionReuseDetectedUrl;

  // ~ Constructors ==========================================================

  public HttpSessionReuseDetectionFilter() {
    super();
  }

  // ~ Methods ===============================================================

  public void init( final FilterConfig filterConfig ) throws ServletException {
  }

  public void doFilter( final ServletRequest request, final ServletResponse response, final FilterChain chain )
    throws IOException, ServletException {
    if ( !( request instanceof HttpServletRequest ) ) {
      throw new ServletException();
    }

    if ( !( response instanceof HttpServletResponse ) ) {
      throw new ServletException();
    }

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    if ( requiresAuthentication( httpRequest, httpResponse ) ) {
      if ( HttpSessionReuseDetectionFilter.logger.isDebugEnabled() ) {
        HttpSessionReuseDetectionFilter.logger.debug( Messages.getInstance().getString(
            "HttpSessionReuseDetectionFilter.DEBUG_PROCESS_AUTHN" ) ); //$NON-NLS-1$
      }

      // TODO: this should use LogoutHandlers in latest Spring Security
        String remoteUser = httpRequest.getRemoteUser();
        if ( ( null != remoteUser ) && ( remoteUser.length() > 0 ) ) {
          if ( HttpSessionReuseDetectionFilter.logger.isDebugEnabled() ) {
            HttpSessionReuseDetectionFilter.logger.debug( Messages.getInstance().getString(
              "HttpSessionReuseDetectionFilter.DEBUG_USER_ALREADY_LOGGED_IN", remoteUser ) ); //$NON-NLS-1$
          }

          HttpSession session = httpRequest.getSession( false );
          if ( null != session ) {
            if ( HttpSessionReuseDetectionFilter.logger.isDebugEnabled() ) {
              HttpSessionReuseDetectionFilter.logger.debug( Messages.getInstance().getString(
                "HttpSessionReuseDetectionFilter.DEBUG_INVALIDATING_SESSION" ) ); //$NON-NLS-1$
            }
            session.invalidate();
          }

          SecurityContextHolder.clearContext();

          if ( HttpSessionReuseDetectionFilter.logger.isDebugEnabled() ) {
            HttpSessionReuseDetectionFilter.logger.debug( Messages.getInstance().getString(
                "HttpSessionReuseDetectionFilter.DEBUG_REDIRECTING", sessionReuseDetectedUrl ) ); //$NON-NLS-1$
          }

          httpResponse.sendRedirect( httpResponse.encodeRedirectURL( httpRequest.getContextPath()
              + sessionReuseDetectedUrl ) );
          return;
        }
      }

    chain.doFilter( request, response );
  }

  public void destroy() {
  }

  public void afterPropertiesSet() throws Exception {
    Assert.hasLength( filterProcessesUrl, Messages.getInstance().getString(
      "HttpSessionReuseDetectionFilter.ERROR_0001_FILTERPROCESSESURL_NOT_SPECIFIED" ) ); //$NON-NLS-1$
    Assert.hasLength( sessionReuseDetectedUrl, Messages.getInstance().getString(
      "HttpSessionReuseDetectionFilter.ERROR_0002_SESSIONREUSEDETECTEDURL_NOT_SPECIFIED" ) ); //$NON-NLS-1$
  }

  /**
   * <p>
   * Indicates whether this filter should attempt to process a login request for the current invocation.
   * </p>
   * 
   * <p>
   * It strips any parameters from the "path" section of the request URL (such as the jsessionid parameter in
   * <em>http://host/myapp/index.html;jsessionid=blah</em>) before matching against the <code>filterProcessesUrl</code>
   * property.
   * </p>
   * 
   * <p>
   * Subclasses may override for special requirements, such as Tapestry integration.
   * </p>
   * 
   * @param request
   *          as received from the filter chain
   * @param response
   *          as received from the filter chain
   * 
   * @return <code>true</code> if the filter should attempt authentication, <code>false</code> otherwise
   */
  protected boolean requiresAuthentication( final HttpServletRequest request, final HttpServletResponse response ) {
    String uri = request.getRequestURI();
    int pathParamIndex = uri.indexOf( ';' );

    if ( pathParamIndex > 0 ) {
      // strip everything after the first semi-colon
      uri = uri.substring( 0, pathParamIndex );
    }

    return uri.endsWith( request.getContextPath() + filterProcessesUrl );
  }

  public String getFilterProcessesUrl() {
    return filterProcessesUrl;
  }

  public void setFilterProcessesUrl( final String filterProcessesUrl ) {
    this.filterProcessesUrl = filterProcessesUrl;
  }

  public String getSessionReuseDetectedUrl() {
    return sessionReuseDetectedUrl;
  }

  public void setSessionReuseDetectedUrl( final String sessionReuseDetectedUrl ) {
    this.sessionReuseDetectedUrl = sessionReuseDetectedUrl;
  }

}
