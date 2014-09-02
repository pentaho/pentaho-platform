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

package org.pentaho.platform.web.http.security;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.Authentication;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.event.authentication.AuthenticationSuccessEvent;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This class's sole purpose is to defeat the persistence of Basic-Auth credentials in the browser. The mechanism used
 * to accomplish this is to detect an expired (invalid) HttpSession from the client.
 * <p/>
 * If the first request after a session becomes invalid is a Basic-Auth request, we automatically deny, forcing
 * re-authentication.
 * <p/>
 * The second path is if the first request after session invalidation is not a basic-auth (user manually logged out and
 * was presented with the login page), we drop a cookie in the response noting the event. The next request with
 * Basic-Auth and a valid HttpSession checks for this cookie and if present, forces reauthentication.
 * <p/>
 * <p/>
 * User: nbaker Date: 8/15/13
 */
public class PentahoBasicProcessingFilter extends org.springframework.security.ui.basicauth.BasicProcessingFilter
    implements ApplicationEventPublisherAware {

  private ApplicationEventPublisher applicationEventPublisher;

  public void setApplicationEventPublisher( ApplicationEventPublisher applicationEventPublisher ) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public void doFilterHttp( HttpServletRequest request, HttpServletResponse response, FilterChain chain )
    throws IOException, ServletException {
    if ( request.getRequestedSessionId() != null && !request.isRequestedSessionIdValid() ) {
      // expired session detected.
      Cookie expiredCookie = null;
      for ( Cookie c : request.getCookies() ) {
        if ( "JSESSIONID".equals( c.getName() ) ) {
          expiredCookie = c; // cache that this definitely is a browser with an expired session.
          break;
        }
      }
      String header = request.getHeader( "Authorization" );
      if ( header != null && header.indexOf( "Basic" ) == 0 ) {
        // Session is expired and a Basic-Auth request is coming in. We'll drop a cookie to note this and force
        // re-authentication
        for ( Cookie c : request.getCookies() ) {
          if ( "session-flushed".equals( c.getName() ) ) {
            c.setMaxAge( 0 );
            response.addCookie( c );
            break;
          }
        }
        // force the prompt for credentials
        getAuthenticationEntryPoint()
            .commence( request, response, new BadCredentialsException( "Clearing Basic-Auth" ) );
        return;
      } else if ( expiredCookie != null ) {
        // Session is expired but this request does not include basic-auth, drop a cookie to keep track of this event.
        Cookie c = new Cookie( "session-flushed", "true" );
        c.setPath( request.getContextPath() != null ? request.getContextPath() : "/" );
        c.setMaxAge( -1 );
        response.addCookie( c );
      }
    } else {
      String header = request.getHeader( "Authorization" );
      if ( header != null && header.indexOf( "Basic" ) == 0
          && SecurityContextHolder.getContext().getAuthentication() == null ) {
        // Session is valid, but Basic-auth is supplied. Check to see if the session end cookie we created is present,
        // if so, force reauthentication.

        Cookie[] cookies;
        cookies = request.getCookies();
        if ( cookies != null ) {
          for ( Cookie c : cookies ) {
            if ( "session-flushed".equals( c.getName() ) ) {
              c.setMaxAge( 0 );
              c.setPath( request.getContextPath() != null ? request.getContextPath() : "/" );
              response.addCookie( c );
              getAuthenticationEntryPoint().commence( request, response,
                  new BadCredentialsException( "Clearing Basic-Auth" ) );
              return;
            }
          }
        }
      }
    }

    super.doFilterHttp( request, response, chain );
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
}
