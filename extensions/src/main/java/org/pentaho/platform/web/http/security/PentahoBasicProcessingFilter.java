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
 * Copyright (c) 2002-2022 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.security;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.SessionCookieConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * This class's sole purpose is to defeat the persistence of Basic-Auth credentials in the browser.
 * The mechanism used to accomplish this is to detect an expired (invalid) HttpSession from the client.
 * <p/>
 * If the first request after a session becomes invalid is a Basic-Auth request, we automatically deny, forcing
 * reauthentication.
 * <p/>
 * The second path is if the first request after session invalidation is not a basic-auth (user manually logged out and
 * was presented with the login page), we drop a cookie in the response noting the event. The next request with
 * Basic-Auth and a valid HttpSession checks for this cookie and if present, forces reauthentication.
 * <p/>
 * <p/>
 * User: nbaker Date: 8/15/13
 */
public class PentahoBasicProcessingFilter extends BasicAuthenticationFilter implements ApplicationEventPublisherAware {

  @VisibleForTesting
  static final String SESSION_FLUSHED_COOKIE_NAME = "session-flushed";

  @VisibleForTesting
  static final String SESSION_ID_COOKIE_NAME = "JSESSIONID";


  private ApplicationEventPublisher applicationEventPublisher;

  public PentahoBasicProcessingFilter( AuthenticationManager authenticationManager,
                                       AuthenticationEntryPoint authenticationEntryPoint ) {
    super( authenticationManager, authenticationEntryPoint );
  }

  public void setApplicationEventPublisher( ApplicationEventPublisher applicationEventPublisher ) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public void doFilterInternal( HttpServletRequest request, HttpServletResponse response, FilterChain chain )
    throws IOException, ServletException {

    if ( request.getRequestedSessionId() != null && !request.isRequestedSessionIdValid() ) {
      // Expired session detected.

      if ( isBasicAuthRequest( request ) ) {
        // Session is expired and a Basic-Auth request is coming in.

        // Clear any session-flushed cookie.
        clearSessionFlushedCookie( request, response );

        // Ignore provided credentials, and force the prompt for (new) credentials.
        getAuthenticationEntryPoint()
          .commence( request, response, new BadCredentialsException( "Clearing Basic-Auth" ) );
        return;
      }

      // Check that this definitely is a browser with an expired session.
      boolean hasExpiredSessionCookie = Arrays.stream( request.getCookies() )
        .anyMatch( cookie -> SESSION_ID_COOKIE_NAME.equals( cookie.getName() ) );

      if ( hasExpiredSessionCookie ) {
        // Session is expired but this request does not include basic-auth,
        // drop a cookie to keep track that session had expired.
        response.addCookie( createSessionFlushedCookie( request ) );
      }
    } else if ( isBasicAuthRequest( request ) && SecurityContextHolder.getContext().getAuthentication() == null ) {
      // Session is valid, not authenticated, and Basic-auth is supplied.
      // Check to see if the session-flushed cookie we created is present, and, if so, force reauthentication for
      // the Pentaho realm.
      if ( clearSessionFlushedCookie( request, response ) ) {
        getAuthenticationEntryPoint()
          .commence( request, response, new BadCredentialsException( "Clearing Basic-Auth" ) );
        return;
      }
    }

    doFilterInternalSuper( request, response, chain );
  }

  @VisibleForTesting
  void doFilterInternalSuper( HttpServletRequest request, HttpServletResponse response, FilterChain chain )
    throws IOException, ServletException {
    super.doFilterInternal( request, response, chain );
  }

  @Override
  protected void onSuccessfulAuthentication( HttpServletRequest request, HttpServletResponse response,
                                             Authentication authResult ) throws IOException {
    super.onSuccessfulAuthentication( request, response, authResult );
    request.getSession().setAttribute( "BasicAuth", "true" );
    if ( applicationEventPublisher != null ) {
      applicationEventPublisher.publishEvent( new AuthenticationSuccessEvent( authResult ) );
    }
  }

  protected boolean isBasicAuthRequest( @NonNull HttpServletRequest request ) {
    String header = request.getHeader( "Authorization" );
    return header != null && header.indexOf( "Basic" ) == 0;
  }

  @NonNull
  protected Cookie createSessionFlushedCookie( @NonNull HttpServletRequest request ) {
    Cookie cookie = new Cookie( SESSION_FLUSHED_COOKIE_NAME, "true" );
    // maxAge: -1 means the cookie is cleared when the web-browser session ends.
    configureSessionFlushedCookie( cookie, request, -1 );
    return cookie;
  }

  protected static void configureSessionFlushedCookie( @NonNull Cookie cookie,
                                                     @NonNull HttpServletRequest request,
                                                     int maxAge ) {
    SessionCookieConfig sessionCookieConfig = request.getServletContext().getSessionCookieConfig();
    cookie.setPath( request.getContextPath() != null ? request.getContextPath() : "/" );
    cookie.setHttpOnly( sessionCookieConfig.isHttpOnly() );
    cookie.setSecure( sessionCookieConfig.isSecure() );
    cookie.setMaxAge( maxAge );
  }

  // Used by PentahoBasicAuthenticationEntryPoint
  static boolean clearSessionFlushedCookie( @NonNull HttpServletRequest request,
                                            @NonNull HttpServletResponse response ) {
    Cookie[] cookies = request.getCookies();
    if ( cookies != null ) {
      for ( Cookie cookie : cookies ) {
        if ( SESSION_FLUSHED_COOKIE_NAME.equals( cookie.getName() ) ) {
          // maxAge: 0 causes the browser to remove the cookie.
          configureSessionFlushedCookie( cookie, request, 0 );
          response.addCookie( cookie );
          return true;
        }
      }
    }

    return false;
  }
}
