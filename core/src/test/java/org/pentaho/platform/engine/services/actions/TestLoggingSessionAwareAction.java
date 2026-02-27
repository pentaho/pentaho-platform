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


package org.pentaho.platform.engine.services.actions;

import org.apache.commons.logging.Log;
import org.pentaho.platform.api.action.ILoggingAction;
import org.pentaho.platform.api.action.ISessionAwareAction;
import org.pentaho.platform.api.engine.IPentahoSession;

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
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setLogger( Log logger ) {
    this.logger = logger;
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
