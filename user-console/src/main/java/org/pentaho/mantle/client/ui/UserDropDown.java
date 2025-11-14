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


package org.pentaho.mantle.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import org.pentaho.mantle.client.commands.ChangePasswordCommand;
import org.pentaho.mantle.client.commands.LogoutCommand;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.ui.xul.gwt.tags.GwtMessageBox;

//This rule is triggered when the class has more than 5 parents. in this case most of the parents are third party classes that can't be changed.
@SuppressWarnings( "squid:S110" )
public class UserDropDown extends CustomDropDown implements RequestCallback {

  private MenuBar menuBar;

  public UserDropDown() {
    super( UserDropDown.getUsername(), null, MODE.MINOR );
    menuBar = new MenuBar( true );
    menuBar.addItem( new MenuItem( Messages.getString( "logout" ), new LogoutCommand() ) );

    final String url = GWT.getHostPageBaseURL() + "api/system/authentication-provider";
    RequestBuilder executableTypesRequestBuilder = new RequestBuilder( RequestBuilder.GET, url );
    executableTypesRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    executableTypesRequestBuilder.setHeader( "accept", "application/json" );
    try {
      executableTypesRequestBuilder.sendRequest( null, this );
    } catch ( RequestException e ) {
      showXulErrorMessage( "Error setting password reset option", e.getMessage() );
    }
    menuBar.addStyleName( "puc-logout-option" );
    setMenuBar( menuBar );
    setupNativeHooks( this );
  }

  public void onError( Request request, Throwable exception ) {
    showXulErrorMessage( "Error setting password reset option", exception.getMessage() );
  }

  public void onResponseReceived( Request request, Response response ) {
    String responseText = response.getText();
    if ( responseText.contains( "\"jackrabbit\"" ) || responseText.contains( "\"super\"" ) ) {
      menuBar.addItem( new MenuItem( Messages.getString( "changePassword" ), new ChangePasswordCommand() ) );
    }
  }

  private void showXulErrorMessage( String title, String message ) {
    GwtMessageBox messageBox = new GwtMessageBox();
    messageBox.setTitle( title );
    messageBox.setMessage( message );
    messageBox.setButtons( new Object[GwtMessageBox.ACCEPT] );
    messageBox.setWidth( 520 );
    messageBox.show();
  }

  private static native void setupNativeHooks( CustomDropDown userDropDown )
  /*-{
    $wnd.closeUserDropDownMenu = function() {
      userDropDown.@org.pentaho.mantle.client.ui.CustomDropDown::hidePopup()();
    }
  }-*/;

  public static native String getUsername()
  /*-{  
    return window.parent.SESSION_NAME;
  }-*/;
}
