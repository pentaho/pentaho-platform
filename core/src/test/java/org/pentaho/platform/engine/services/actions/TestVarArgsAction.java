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

import org.pentaho.platform.api.action.IVarArgsAction;

import java.util.Map;

@SuppressWarnings( "nls" )
public class TestVarArgsAction implements IVarArgsAction {

  private String message;
  private boolean executeWasCalled = false;
  private Map<String, Object> varArgs;

  public String getMessage() {
    return message;
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  public boolean isExecuteWasCalled() {
    return executeWasCalled;
  }

  public void execute() throws Exception {
    executeWasCalled = true;
  }

  public void setVarArgs( Map<String, Object> args ) {
    this.varArgs = args;
  }

  public Map<String, Object> getVarArgs() {
    return this.varArgs;
  }
}
