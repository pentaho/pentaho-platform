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


package org.pentaho.mantle.client.usersettings;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.UserSettingsLoadedEvent;
import org.pentaho.mantle.client.messages.Messages;

public class UserSettingsManager {

  private JsArray<JsSetting> settings;
  private static UserSettingsManager instance;

  private UserSettingsManager() {
  }

  public static UserSettingsManager getInstance() {
    if ( instance == null ) {
      instance = new UserSettingsManager();
    }
    return instance;
  }

  public void getUserSettings( final boolean forceReload ) {
    if ( forceReload || settings == null ) {
      getUserSettings( null );
    }
  }

  public void getUserSettings( final AsyncCallback<JsArray<JsSetting>> callback, final boolean forceReload ) {
    if ( forceReload || settings == null ) {
      getUserSettings( callback );
    } else {
      callback.onSuccess( settings );
    }
  }

  private void getUserSettings( final AsyncCallback<JsArray<JsSetting>> callback ) {
    final String url = GWT.getHostPageBaseURL() + "api/user-settings/list"; //$NON-NLS-1$
    RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, url );
    builder.setHeader( "accept", "application/json" );
    builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );

    try {
      builder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          MessageDialogBox dialog =
              new MessageDialogBox(
                  Messages.getString( "error" ), Messages.getString( "couldNotGetUserSettings" ), true, false, true ); //$NON-NLS-1$ //$NON-NLS-2$
          dialog.center();
        }

        public void onResponseReceived( Request request, Response response ) {
          JsArray<JsSetting> jsSettings = null;
          try {
            jsSettings = JsSetting.parseSettingsJson( response.getText() );
          } catch ( Throwable t ) {
            // happens when there are no settings
          }
          //CHECKSTYLE IGNORE Indentation FOR NEXT 1 LINES
          getInstance().settings = jsSettings;
          if ( callback != null ) {
            callback.onSuccess( settings );
          }
          EventBusUtil.EVENT_BUS.fireEvent( new UserSettingsLoadedEvent( settings ) );
        }

      } );
    } catch ( RequestException e ) {
      // showError(e);
    }

  }

}
