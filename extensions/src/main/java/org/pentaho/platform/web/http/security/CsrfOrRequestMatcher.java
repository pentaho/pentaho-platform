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

public class CsrfOrRequestMatcher implements RequestMatcher {
  private static final String CSRF_PROTECTION_ENABLED = "csrf-protection-enabled";

  private boolean csrfProtectionEnabled;
  private List<RequestMatcher> requestMatchers;

  public CsrfOrRequestMatcher( List<RequestMatcher> requestMatchers ) {
    if ( requestMatchers == null ) {
      throw new IllegalArgumentException( "requestMatchers must be defined" );
    }

    this.csrfProtectionEnabled = Boolean.valueOf(
      PentahoSystem.getSystemSetting( CSRF_PROTECTION_ENABLED, "false" ) );

    this.requestMatchers = requestMatchers;
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
