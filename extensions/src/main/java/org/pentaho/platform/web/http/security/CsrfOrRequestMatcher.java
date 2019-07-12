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
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 *
 */
package org.pentaho.platform.web.http.security;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * A request matcher class which matches a given request if any one of a given list of request matchers
 * matches a request.
 *
 * Additionally, the matcher is enabled or disabled according to
 * the Pentaho system's setting "csrf-protection-enabled".
 */
public class CsrfOrRequestMatcher implements RequestMatcher {

  final private boolean csrfProtectionEnabled;
  final private List<RequestMatcher> requestMatchers;

  /**
   * Creates a request matcher given a list of request matchers.
   *
   * @param requestMatchers
   *  The list of request matchers to which actual matching is delegated.
   */
  public CsrfOrRequestMatcher( List<RequestMatcher> requestMatchers ) {
    this( requestMatchers, PentahoSystem.isCsrfProtectionEnabled() );
  }

  // Package private for testing purposes.
  /**
   * Creates a request matcher given a list of request matchers and
   * an indication of whether matching is enabled.
   *
   * @param requestMatchers
   *  The list of request matchers to which actual matching is delegated.
   *
   * @param csrfProtectionEnabled
   *  Indicates if CSRF protection is enabled.
   */
  CsrfOrRequestMatcher( List<RequestMatcher> requestMatchers, boolean csrfProtectionEnabled ) {
    if ( requestMatchers == null ) {
      throw new IllegalArgumentException( "requestMatchers must be defined" );
    }

    this.requestMatchers = requestMatchers;

    this.csrfProtectionEnabled = csrfProtectionEnabled;
  }

  @Override
  public boolean matches( HttpServletRequest request ) {
    boolean match = false;

    if ( this.csrfProtectionEnabled ) {
      for ( RequestMatcher matcher : this.requestMatchers ) {
        if ( matcher.matches( request ) ) {
          match = true;

          break;
        }
      }
    }

    return match;
  }
}
