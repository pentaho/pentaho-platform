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
    //Is triggered either by a page reload or by a timer
    if ( timer != null ) {
      timer.cancel();
      timer = null;
    }
    final int nextCheckShift = getNextCheckShift();
    if ( nextCheckShift < 0 ) {
      new SessionExpiredDialog().center();
    } else {
      timer = new Timer() {
        @Override public void run() {
          performOperation( true );
        }
      };
      timer.schedule( nextCheckShift );
    }
  }

  public Integer getPollingInterval() {
    return pollingInterval;
  }

  public void setPollingInterval( final Integer pollingInterval ) {
    this.pollingInterval = pollingInterval;
  }


  /**
   * If the session is expired returns a negative value.
   * If the session is not expired returns a time left before expiration.
   * If the cookie is not set returns a default polling interval.
   *
   * @return time shift for the next check
   */
  private int getNextCheckShift() {
    final String sessionExpiry = Cookies.getCookie( SESSION_EXPIRY );
    //A cookie is not set
    if ( sessionExpiry != null ) {
      try {
        final long timeLeft = Long.parseLong( sessionExpiry ) - System.currentTimeMillis();
        if ( timeLeft <= 0 ) {
          //Session is expired
          return -1; //do not overflow
        } else if ( timeLeft < Integer.MAX_VALUE ) { //do not overflow
          //Not expired
          return (int) ( timeLeft );
        }
      } catch ( NumberFormatException e ) {
        //wrong value in a cookie
      }
    }

    //No cookie - use a default interval
    return pollingInterval;
  }
}
