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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.MantleSettingsLoadedEvent;
import org.pentaho.mantle.client.messages.Messages;

import java.util.HashMap;

public class MantleSettingsManager {

  private HashMap<String, String> settings = new HashMap<String, String>();
  private boolean isAdministrator = false;

  private static MantleSettingsManager instance;

  private MantleSettingsManager() {
  }

  public static MantleSettingsManager getInstance() {
    if ( instance == null ) {
      instance = new MantleSettingsManager();
    }
    return instance;
  }

  public void getMantleSettings( final boolean forceReload ) {
    if ( forceReload || settings.size() == 0 ) {
      getMantleSettings( null );
    }
  }

  public void getMantleSettings( final AsyncCallback<HashMap<String, String>> callback, final boolean forceReload ) {
    if ( forceReload || settings.size() == 0 ) {
      getMantleSettings( callback );
    } else {
      callback.onSuccess( settings );
    }
  }

  private void getMantleSettings( final AsyncCallback<HashMap<String, String>> callback ) {
    final RequestCallback internalCallback = new RequestCallback() {

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

        for ( int i = 0; i < jsSettings.length(); i++ ) {
          settings.put( jsSettings.get( i ).getName(), jsSettings.get( i ).getValue() );
        }

        settings.put( "is-administrator", "" + isAdministrator );
        if ( callback != null ) {
          callback.onSuccess( settings );
        }
        EventBusUtil.EVENT_BUS.fireEvent( new MantleSettingsLoadedEvent( settings ) );
      }
    };

    final RequestBuilder builder =
        new RequestBuilder( RequestBuilder.GET, GWT.getHostPageBaseURL() + "api/mantle/settings" );
    builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    builder.setHeader( "accept", "application/json" );

    try {
      final String url = GWT.getHostPageBaseURL() + "api/repo/files/canAdminister"; //$NON-NLS-1$
      RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, url );
      builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      requestBuilder.setHeader( "accept", "text/plain" );
      requestBuilder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable caught ) {
          try {
            builder.sendRequest( null, internalCallback );
          } catch ( RequestException e ) {
            //ignored
          }
          //CHECKSTYLE IGNORE Indentation FOR NEXT 1 LINES
          MantleSettingsManager.getInstance().isAdministrator = false;
        }

        public void onResponseReceived( Request request, Response response ) {
          try {
            builder.sendRequest( null, internalCallback );
          } catch ( RequestException e ) {
            //ignored
          }
          //CHECKSTYLE IGNORE Indentation FOR NEXT 1 LINES
          MantleSettingsManager.getInstance().isAdministrator = isAdministrator;
        }

      } );
    } catch ( RequestException e ) {
      Window.alert( e.getMessage() );
    }
  }

}
