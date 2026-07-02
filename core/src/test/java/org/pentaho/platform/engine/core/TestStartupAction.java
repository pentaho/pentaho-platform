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


package org.pentaho.platform.engine.core;

import org.pentaho.platform.api.engine.ISessionStartupAction;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class TestStartupAction implements ISessionStartupAction {

  public String getActionOutputScope() {
    // TODO Auto-generated method stub
    return PentahoSystem.SCOPE_SESSION;
  }

  public String getActionPath() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getSessionType() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setActionOutputScope( String arg0 ) {
    // TODO Auto-generated method stub

  }

  public void setActionPath( String arg0 ) {
    // TODO Auto-generated method stub

  }

  public void setSessionType( String arg0 ) {
    // TODO Auto-generated method stub

  }

}
