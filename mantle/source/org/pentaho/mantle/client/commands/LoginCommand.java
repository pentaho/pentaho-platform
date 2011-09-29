package org.pentaho.mantle.client.commands;

import com.google.gwt.core.client.JavaScriptObject;

public class LoginCommand extends AbstractCommand {

  public LoginCommand() {
    super();
  }

  protected void performOperation() {
    performOperation(false);
  }

  protected void performOperation(boolean feedback) {
  }

  public void loginWithCallback(final JavaScriptObject obj) {
    execute(new CommandCallback() {

      public void afterExecute() {
        executeNativeCallback(obj);
      }
    });
  }

  private native void executeNativeCallback(JavaScriptObject obj)
  /*-{
    try {
      obj.loginCallback();
    } catch (e){}
  }-*/;
}
