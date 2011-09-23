package org.pentaho.test.platform.engine.services.actions;

import org.apache.commons.logging.Log;
import org.pentaho.platform.api.action.ILoggingAction;
import org.pentaho.platform.api.action.ISessionAwareAction;
import org.pentaho.platform.api.engine.IPentahoSession;

@SuppressWarnings("nls")
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
    logger.error("Test Error Message");
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setLogger(Log logger) {
    this.logger = logger;
    logger.warn("Test Warning Message");
  }
  
  public Log getLogger() {
    return logger;
  }

  public void setSession(IPentahoSession session) {
    this.session = session;
  }
  
  public IPentahoSession getSession() {
    return session;
  }
}
