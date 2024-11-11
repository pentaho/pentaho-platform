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
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.csrf.CsrfRequestBuilder;
import org.pentaho.mantle.client.messages.Messages;

public class ExecuteGlobalActionsCommand extends AbstractCommand {

  public ExecuteGlobalActionsCommand() {
  }

  protected void performOperation() {
    final String url = GWT.getHostPageBaseURL() + "api/system/refresh/globalActions"; //$NON-NLS-1$
    RequestBuilder requestBuilder = new CsrfRequestBuilder( RequestBuilder.GET, url );
    requestBuilder.setHeader( "accept", "text/plain" );
    requestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    try {
      requestBuilder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          // showError(exception);
        }

        public void onResponseReceived( Request request, Response response ) {
          MessageDialogBox dialogBox =
              new MessageDialogBox(
                  Messages.getString( "info" ), Messages.getString( "globalActionsExecutedSuccessfully" ), false, false, true ); //$NON-NLS-1$ //$NON-NLS-2$
          dialogBox.center();
        }
      } );
    } catch ( RequestException e ) {
      Window.alert( e.getMessage() );
      // showError(e);
    }
  }

  protected void performOperation( final boolean feedback ) {
    // do nothing
  }
}
