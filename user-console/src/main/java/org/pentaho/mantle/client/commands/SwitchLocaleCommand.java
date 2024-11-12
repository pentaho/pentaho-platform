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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

public class SwitchLocaleCommand extends AbstractCommand {

  private String locale;

  public SwitchLocaleCommand() {
  }

  public SwitchLocaleCommand( String locale ) {
    this.locale = locale;
  }

  protected void performOperation() {
    performOperation( true );
  }

  protected void performOperation( boolean feedback ) {
    String newLocalePath = "Home?locale=" + locale;

    String baseUrl = GWT.getModuleBaseURL();
    int index = baseUrl.indexOf( "/mantle/" );
    if ( index >= 0 ) {
      String basePath = baseUrl.substring( 0, index );
      newLocalePath = basePath + "/" + newLocalePath;
    }

    Window.Location.replace( newLocalePath );
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale( String locale ) {
    this.locale = locale;
  }
}
