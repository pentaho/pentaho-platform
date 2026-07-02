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


package org.pentaho.platform.engine.core.system;

import org.pentaho.platform.api.engine.ISessionStartupAction;

public class SessionStartupAction implements ISessionStartupAction {
  String sessionType;
  String actionOutputScope;
  String actionPath;

  public String getActionOutputScope() {
    return actionOutputScope;
  }

  public void setActionOutputScope( String actionOutputScope ) {
    this.actionOutputScope = actionOutputScope;
  }

  public String getActionPath() {
    return actionPath;
  }

  public void setActionPath( String actionPath ) {
    this.actionPath = actionPath;
  }

  public String getSessionType() {
    return sessionType;
  }

  public void setSessionType( String sessionType ) {
    this.sessionType = sessionType;
  }
}
