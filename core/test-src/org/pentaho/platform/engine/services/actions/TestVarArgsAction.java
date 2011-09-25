package org.pentaho.platform.engine.services.actions;

import java.util.Map;

import org.pentaho.platform.api.action.IVarArgsAction;

@SuppressWarnings("nls")
public class TestVarArgsAction implements IVarArgsAction {

  private String message;
  private boolean executeWasCalled = false;
  private Map<String, Object> varArgs;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public boolean isExecuteWasCalled() {
    return executeWasCalled;
  }

  public void execute() throws Exception {
    executeWasCalled = true;
  }

  public void setVarArgs(Map<String, Object> args) {
    this.varArgs = args;
  }
  
  public Map<String, Object> getVarArgs() {
    return this.varArgs;
  }
}
