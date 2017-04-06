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
 * Copyright (c) 2017 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.mantle.client.commands;

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import org.pentaho.gwt.widgets.client.dialogs.SessionExpiredDialog;

/**
 * Shows a session expired dialog in a top frame.
 * Won't work on the screens missing the mantle application.
 */
public class SessionExpiredCommand extends AbstractCommand {

  //10 seconds by default
  private Integer pollingInterval = 10000;
  private static final String SESSION_EXPIRY = "session-expiry";
  private static Timer timer;

  @Override protected void performOperation() {
    this.performOperation( true );
  }

  @Override protected void performOperation( boolean feedback ) {
    checkCookie();
    //Only one timer at a time
    if ( timer == null ) {
      timer = new Timer() {
        @Override public void run() {
          checkCookie();
        }
      };
      timer.scheduleRepeating( getPollingInterval() );
    }
  }

  public Integer getPollingInterval() {
    return pollingInterval;
  }

  public void setPollingInterval( final Integer pollingInterval ) {
    this.pollingInterval = pollingInterval;
  }

  /**
   * Checks the cookie set in {@link org.pentaho.platform.web.http.filters
   * .HttpSessionPentahoSessionIntegrationFilter#setSessionExpirationCookies(HttpSession,
   * HttpServletResponse)}
   */
  private void checkCookie() {
    final String sessionExpiry = Cookies.getCookie( SESSION_EXPIRY );
    //A cookie is not set
    if ( sessionExpiry != null ) {
      try {
        if ( System.currentTimeMillis() > Long.parseLong( sessionExpiry ) ) {
          //Session expired
          new SessionExpiredDialog().center();
          timer.cancel();
          timer = null;
        }
      } catch ( final NumberFormatException e ) {
        //Wrong value in the header
      }
    }
  }
}
