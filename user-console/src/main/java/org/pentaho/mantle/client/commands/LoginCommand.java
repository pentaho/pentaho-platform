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

import com.google.gwt.core.client.JavaScriptObject;

public class LoginCommand extends AbstractCommand {

  public LoginCommand() {
    super();
  }

  protected void performOperation() {
    performOperation( false );
  }

  protected void performOperation( boolean feedback ) {
  }

  public void loginWithCallback( final JavaScriptObject obj ) {
    execute( new CommandCallback() {

      public void afterExecute() {
        executeNativeCallback( obj );
      }
    } );
  }

  private native void executeNativeCallback( JavaScriptObject obj )
  /*-{
    try {
      obj.loginCallback();
    } catch (e){}
  }-*/;
}
