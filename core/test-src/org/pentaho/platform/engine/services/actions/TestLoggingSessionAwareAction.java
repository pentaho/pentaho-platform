/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services.actions;

import org.apache.commons.logging.Log;
import org.pentaho.platform.api.action.ILoggingAction;
import org.pentaho.platform.api.action.ISessionAwareAction;
import org.pentaho.platform.api.engine.IPentahoSession;

@SuppressWarnings( "nls" )
public class TestLoggingSessionAwareAction implements ILoggingAction, ISessionAwareAction {

  private Log logger;
  private String message;
  private boolean executeWasCalled = false;
  private IPentahoSession session;

  public boolean isExecuteWasCalled() {
    return executeWasCalled;
  }

  public void execute() throws Exception {
    executeWasCalled = true;
    logger.error( "Test Error Message" );
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setLogger( Log logger ) {
    this.logger = logger;
    logger.warn( "Test Warning Message" );
  }

  public Log getLogger() {
    return logger;
  }

  public void setSession( IPentahoSession session ) {
    this.session = session;
  }

  public IPentahoSession getSession() {
    return session;
  }
}
