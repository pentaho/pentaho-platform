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


package org.pentaho.mantle.client.commands;

import com.google.gwt.thirdparty.guava.common.annotations.VisibleForTesting;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import org.pentaho.gwt.widgets.client.dialogs.SessionExpiredDialog;

/**
 * Shows a session expired dialog in a top frame.
 * Won't work on the screens missing the mantle application.
 */
public class SessionExpiredCommand extends AbstractCommand {

  private static final String ZERO = "0";
  //10 seconds by default
  private Integer pollingInterval = 10000;
  private static final String SESSION_EXPIRY = "session-expiry";
  private static final String SERVER_TIME = "server-time";
  private static final String CLIENT_TIME_OFFSET = "client-time-offset";
  private static Timer timer;


  @Override protected void performOperation() {
    this.performOperation( true );
  }

  @Override protected void performOperation( boolean feedback ) {
    //Reset and reinitialize timer once per page load
    if ( timer != null ) {
      timer.cancel();
      timer = null;
    }
    //Calculate and set client/server time desynchronization offset once per page load
    setClientTimeOffset();
    performCheck();
  }


  /**
   * A timer loop
   */
  private void performCheck() {
    final int nextCheckShift = getNextCheckShift();
    if ( nextCheckShift < 0 ) {
      new SessionExpiredDialog().center();
    } else {
      timer = new Timer() {
        @Override public void run() {
          performCheck();
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
  @VisibleForTesting
  protected int getNextCheckShift() {
    final String sessionExpiry = getCookie( SESSION_EXPIRY );
    final String clientTimeOffset = getCookie( CLIENT_TIME_OFFSET );
    //A cookie is not set
    if ( sessionExpiry != null && clientTimeOffset != null ) {
      try {
        final long timeLeft =
          Long.parseLong( sessionExpiry ) - getClientTime() + Long.parseLong( clientTimeOffset );
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
    return getPollingInterval();
  }

  @VisibleForTesting
  protected long getClientTime() {
    return System.currentTimeMillis();
  }

  /**
   * Don't be fooled with a System.currentTimeMillis() - in GWT it's a client side time. To eliminate a possible client
   * clock desynchronization effect we need to calculate an offset between client and server time.
   * Also there always will be a small positive offset equal to a time between the page load nad servlet invocation.
   * <p>
   * In case of any unpredicted situation the offset is set to 0.
   */
  @VisibleForTesting
  protected void setClientTimeOffset() {
    final String serverTime = getCookie( SERVER_TIME );
    if ( serverTime != null ) {
      try {
        //Could be negative
        final long timeOffset = getClientTime() - Long.parseLong( serverTime );
        setCookie( CLIENT_TIME_OFFSET, String.valueOf( timeOffset ) );
      } catch ( NumberFormatException e ) {
        setCookie( CLIENT_TIME_OFFSET, ZERO );
      }
    } else {
      setCookie( CLIENT_TIME_OFFSET, ZERO );
    }
  }

  @VisibleForTesting
  protected String getCookie( final String name ) {
    return Cookies.getCookie( name );
  }

  @VisibleForTesting
  protected void setCookie( final String name, final String value ) {
    Cookies.setCookie( name, value );
  }

}
