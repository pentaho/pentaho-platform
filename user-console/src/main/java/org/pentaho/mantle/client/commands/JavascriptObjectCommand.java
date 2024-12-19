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

/**
 * User: nbaker Date: Sep 23, 2010
 */
public class JavascriptObjectCommand extends AbstractCommand {

  private JavaScriptObject func;

  public JavascriptObjectCommand() {
  }

  public JavascriptObjectCommand( JavaScriptObject func ) {
    this.func = func;
  }

  @Override
  protected void performOperation() {
    performOperation( true );
  }

  @Override
  protected void performOperation( boolean feedback ) {
    execFunc( func );
  }

  private native void execFunc( JavaScriptObject func )
  /*-{
    func();
  }-*/;
}
