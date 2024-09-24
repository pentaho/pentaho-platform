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
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.core.audit;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.slf4j.MDC;

/**
 * MDCUtil is a helper class for managing and copying MDC context across threads so that we can track user/session/IP
 * information for sensitive data access on log statements.
 *
 * @author benny
 */
public class MDCUtil {

  public static final String SESSION_ID = "sessionId";
  public static final String REMOTE_ADDR = "remoteAddr";
  public static final String REMOTE_HOST = "remoteHost";
  public static final String REMOTE_PORT = "remotePort";
  public static final String SERVER_NAME = "serverName";
  public static final String SERVER_PORT = "serverPort";
  public static final String LOCAL_ADDR = "localAddr";
  public static final String LOCAL_NAME = "localName";
  public static final String LOCAL_PORT = "localPort";
  public static final String SESSION_NAME = "sessionName";
  public static final String INSTANCE_ID = "instanceId";

  private final Map<String, String> mdc = new HashMap<>();

  /**
   * Constructor is called on parent thread so a snapshot of the MDC context is saved here.
   * 
   */
  public MDCUtil() {
    Map<String, String> map = MDC.getCopyOfContextMap();
    if ( map != null ) {
      this.mdc.putAll( map );
    }
  }

  /**
   * Sets the instanceId into the current MDC context.
   * 
   * instanceId usually corresponds to an execution of a report
   * 
   * @param instanceId
   */
  public static void setInstanceId( String instanceId ) {
    if ( instanceId != null ) {
      MDC.put( INSTANCE_ID, instanceId );
    }
  }

  public static String getInstanceId() {
    return MDC.get( INSTANCE_ID );
  }

  /**
   * Sets up the MDC context for threads in the scheduler.
   * 
   * @param sessionName
   * @param instanceId
   */
  public static void setupSchedulerMDC( Object sessionName, Object instanceId ) {
    MDC.clear();
    if ( PentahoSessionHolder.getSession() != null && PentahoSessionHolder.getSession().getId() != null ) {
      MDC.put( SESSION_ID, PentahoSessionHolder.getSession().getId() );
    }
    if ( sessionName != null ) {
      MDC.put( SESSION_NAME, sessionName.toString() );
    }
    if ( instanceId != null ) {
      MDC.put( INSTANCE_ID, instanceId.toString() );
    }
  }

  /**
   * This is called on a child thread to update the child thread's MDC context with the caller/parent thread's MDC
   * context.
   * 
   */
  public void setContextMap() {
    MDC.setContextMap( mdc );
  }

  public static void clear() {
    MDC.clear();
  }

  public Map<String, String> getContextMap() {
    return mdc;
  }

}
